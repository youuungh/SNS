package com.ninezero.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.ninezero.data.db.PostDatabase
import com.ninezero.data.db.RemoteKey
import com.ninezero.data.model.param.ContentParam
import com.ninezero.data.model.param.PostParam
import com.ninezero.data.model.parcel.PostParcel
import com.ninezero.data.ktor.PostService
import com.ninezero.domain.model.ACTION_POSTED
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.FileUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json
import timber.log.Timber

/** retrofit
@HiltWorker
class PostWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val fileUseCase: FileUseCase,
    private val postService: PostService
) : CoroutineWorker(appContext.applicationContext, params) {

    companion object {
        const val CHANNEL_ID = "게시글 업로드"
        const val CHANNEL_NAME = "게시글 업로드 채널"
        const val FOREGROUND_NOTIFICATION_ID = 1000
        // const val CHAT_NOTIFICATION_ID = 2000
    }

    override suspend fun doWork(): Result {
        val postParcelJson =
            inputData.getString(PostParcel::class.java.simpleName) ?: return Result.failure()
        val postParcel = Json.decodeFromString<PostParcel>(postParcelJson)

        return try {
            setForeground(getForegroundInfo())
            createPost(postParcel)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(FOREGROUND_NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "백그라운드에서 게시물을 업로드합니다"
        }
        val notificationManager = appContext.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("게시물 업로드")
            .setContentText("게시물을 업로드하는 중입니다...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private suspend fun createPost(postParcel: PostParcel) {
        val uploadedImages = postParcel.images.mapNotNull { imageParcel ->
            val image = imageParcel.toImage()
            when (val result = fileUseCase.uploadImage(image)) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Timber.e("이미지 업로드 실패: ${result.message}")
                    return@mapNotNull null
                }
            }
        }

        if (uploadedImages.size != postParcel.images.size) {
            Timber.e("일부 이미지 업로드 실패")
            throw Exception("이미지 업로드 실패")
        }

        val contentParam = ContentParam(
            text = postParcel.content,
            images = uploadedImages
        )

        val postParam = PostParam(
            title = postParcel.title,
            content = contentParam.toJson()
        )

        val response = postService.createPost(postParam.toRequestBody())
        if (response.result == "SUCCESS") {
            appContext.sendBroadcast(
                Intent(ACTION_POSTED).apply { setPackage(appContext.packageName) }
            )
        } else {
            throw Exception("게시글 생성 실패")
        }
    }
}
*/

@HiltWorker
class PostWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val fileUseCase: FileUseCase,
    private val postService: PostService,
    private val database: PostDatabase
) : CoroutineWorker(appContext.applicationContext, params) {

    companion object {
        const val CHANNEL_ID = "게시글 업로드"
        const val CHANNEL_NAME = "게시글 업로드 채널"
        const val FOREGROUND_NOTIFICATION_ID = 1000
    }

    override suspend fun doWork(): Result {
        val postParcelJson =
            inputData.getString(PostParcel::class.java.simpleName) ?: return Result.failure()
        val postParcel = Json.decodeFromString<PostParcel>(postParcelJson)

        return try {
            setForeground(getForegroundInfo())
            createPost(postParcel)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                FOREGROUND_NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(
                FOREGROUND_NOTIFICATION_ID,
                createNotification()
            )
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "백그라운드에서 게시물을 업로드합니다"
        }
        val notificationManager = appContext.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("게시물 업로드")
            .setContentText("게시물을 업로드하는 중입니다...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private suspend fun createPost(postParcel: PostParcel) {
        val uploadedImages = postParcel.images.mapNotNull { imageParcel ->
            val image = imageParcel.toImage()
            when (val result = fileUseCase.uploadImage(image)) {
                is ApiResult.Success -> result.data
                is ApiResult.Error -> {
                    Timber.e("이미지 업로드 실패: ${result.message}")
                    null
                }
            }
        }

        if (uploadedImages.size != postParcel.images.size) {
            Timber.e("일부 이미지 업로드 실패")
            throw Exception("이미지 업로드 실패")
        }

        val contentParam = ContentParam(
            text = postParcel.content,
            images = uploadedImages
        )

        val postParam = PostParam(
            title = postParcel.title,
            content = contentParam.toJson()
        )

        val response = postService.createPost(postParam)
        if (response.result == "SUCCESS") {
            // DB 새 게시물 추가
            database.withTransaction {
                val posts = postService.getPosts(page = 1, size = 1).data
                if (posts?.isNotEmpty() == true) {
                    database.postDao().insertAll(posts)
                }
            }

//            appContext.sendBroadcast(
//                Intent(ACTION_POSTED).apply { setPackage(appContext.packageName) }
//            )
        } else {
            throw Exception("게시글 생성 실패")
        }
    }
}