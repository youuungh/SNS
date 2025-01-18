package com.ninezero.data.usecase

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.ninezero.data.di.BASE_URL
import com.ninezero.data.ktor.FileService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Image
import com.ninezero.domain.usecase.FileUseCase
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

/** retrofit
class FileUseCaseImpl @Inject constructor(
    private val context: Context,
    private val fileService: FileService
) : FileUseCase {

    companion object {
        private val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )
    }

    override suspend fun getImage(uri: String): Image? {
        val contentUri = Uri.parse(uri)
        val cursor = context.contentResolver.query(
            contentUri,
            IMAGE_PROJECTION,
            null,
            null,
            null
        )
        return cursor?.use { c ->
            c.moveToNext()
            val nameIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeTypeIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val name = cursor.getString(nameIndex)
            val size = cursor.getLong(sizeIndex)
            val mimeType = cursor.getString(mimeTypeIndex)
            Image(
                uri = uri,
                name = name,
                size = size,
                mimeType = mimeType
            )
        }
    }

    override suspend fun uploadImage(image: Image): ApiResult<String> = try {
        val fileNamePart = MultipartBody.Part.createFormData(
            "fileName",
            image.name
        )

        val requestBody = UriRequestBody(
            getInputStream = { getInputStream(image.uri) },
            contentType = image.mimeType.toMediaType(),
            contentLength = image.size
        )

        val filePart = MultipartBody.Part.createFormData(
            "file",
            image.name,
            requestBody
        )

        val response = fileService.uploadFile(
            fileName = fileNamePart,
            file = filePart
        )
        ApiResult.Success(BASE_URL + response.data.filePath)
    } catch (e: Exception) {
        Timber.e(e)
        ApiResult.Error.ServerError("이미지 업로드에 실패했습니다")
    }

    private fun getInputStream(contentUri: String): Result<InputStream> = runCatching {
        val uri = Uri.parse(contentUri)
        context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("InputStream 얻기 실패")
    }
}
*/

class FileUseCaseImpl @Inject constructor(
    private val context: Context,
    private val fileService: FileService
) : FileUseCase {

    companion object {
        private val IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )
    }

    override suspend fun getImage(uri: String): Image? {
        val contentUri = Uri.parse(uri)
        val cursor = context.contentResolver.query(
            contentUri,
            IMAGE_PROJECTION,
            null,
            null,
            null
        )
        return cursor?.use { c ->
            c.moveToNext()
            val nameIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeTypeIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val name = cursor.getString(nameIndex)
            val size = cursor.getLong(sizeIndex)
            val mimeType = cursor.getString(mimeTypeIndex)
            Image(
                uri = uri,
                name = name,
                size = size,
                mimeType = mimeType
            )
        }
    }

    override suspend fun uploadImage(image: Image): ApiResult<String> {
        return try {
            Timber.d("Starting image upload - Name: ${image.name}, Size: ${image.size}")

            // URI에서 ByteArray로 변환
            val imageBytes = getInputStream(image.uri).getOrNull()
                ?.use { it.readBytes() }
                ?: return ApiResult.Error.InvalidRequest("이미지 데이터를 읽을 수 없습니다")

            val response = fileService.uploadFile(
                fileName = image.name,
                fileBytes = imageBytes
            )

            if (response.result == "SUCCESS") {
                ApiResult.Success(BASE_URL + response.data!!.filePath)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "이미지 업로드에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    private fun getInputStream(contentUri: String): Result<InputStream> = runCatching {
        val uri = Uri.parse(contentUri)
        context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("InputStream 얻기 실패")
    }
}