package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.FileDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import javax.inject.Inject

/** retrofit
interface FileService {

    @POST("files")
    @Multipart
    @Headers("ContentType: multipart/form-data;")
    suspend fun uploadFile(
        @Part fileName: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): CommonResponse<FileDto>
}
*/

class FileService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun uploadFile(fileName: String, fileBytes: ByteArray): CommonResponse<FileDto> {
        // 파일의 MIME 타입 추론
        val mimeType = when {
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".gif") -> "image/gif"
            fileName.endsWith(".webp") -> "image/webp"
            else -> "application/octet-stream"
        }

        return client.submitFormWithBinaryData(
            url = "files",
            formData = formData {
                append("fileName", fileName)
                append(
                    "file",
                    fileBytes,
                    headersOf(
                        HttpHeaders.ContentType to listOf(mimeType),
                        HttpHeaders.ContentDisposition to listOf("filename=$fileName")
                    )
                )
            }
        ).body()
    }
}