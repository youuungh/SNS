package com.ninezero.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.ninezero.data.model.ContentParam
import com.ninezero.data.model.PostParam
import com.ninezero.data.model.PostParcel
import com.ninezero.data.retrofit.PostService
import com.ninezero.domain.model.ACTION_POSTED
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.usecase.FileUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.json.Json
import timber.log.Timber

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