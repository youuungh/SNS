package com.ninezero.data.usecase

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ninezero.data.model.parcel.ImageParcel
import com.ninezero.data.model.parcel.PostParcel
import com.ninezero.data.worker.PostWorker
import com.ninezero.domain.model.Image
import com.ninezero.domain.usecase.PostUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PostUseCaseImpl @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager
) : PostUseCase {

    companion object {
        private val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )
    }

    override suspend fun getImageList(): List<Image> = withContext(IO) {
        val contentResolver = context.contentResolver

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val images = mutableListOf<Image>()

        contentResolver.query(
            collectionUri,
            IMAGE_PROJECTION,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collectionUri, id)
                val name = cursor.getString(displayNameColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                images.add(
                    Image(
                        uri = uri.toString(),
                        name = name,
                        size = size,
                        mimeType = mimeType
                    )
                )
            }
        }

        return@withContext images
    }

    override suspend fun createPost(
        title: String,
        content: String,
        images: List<Image>
    ) {
        val postParcel = PostParcel(
            title = title,
            content = content,
            images = images.map { ImageParcel.fromImage(it) }
        )

        val postParcelJson = Json.encodeToString(postParcel)

        val workRequest = OneTimeWorkRequestBuilder<PostWorker>()
            .setInputData(
                workDataOf(PostParcel::class.java.simpleName to postParcelJson)
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueue(workRequest)
    }
}