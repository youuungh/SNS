//package com.ninezero.data.temp
//
//import android.R
//import android.annotation.SuppressLint
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Intent
//import android.content.pm.ServiceInfo
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.lifecycle.LifecycleService
//import androidx.lifecycle.lifecycleScope
//import com.ninezero.data.model.ContentParam
//import com.ninezero.data.model.PostParam
//import com.ninezero.data.model.PostParcel
//import com.ninezero.data.retrofit.PostService
//import com.ninezero.domain.model.ACTION_POSTED
//import com.ninezero.domain.model.ApiResult
//import com.ninezero.domain.usecase.FileUseCase
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.Dispatchers.IO
//import kotlinx.coroutines.launch
//import timber.log.Timber
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class PostingService : LifecycleService() {
//
//    @Inject
//    lateinit var fileUseCase: FileUseCase
//
//    @Inject
//    lateinit var postService: PostService
//
//    companion object {
//        const val EXTRA_POST = "extra_post"
//        const val CHANNEL_ID = "게시글 업로드"
//        const val CHANNEL_NAME = "게시글 업로드 채널"
//        const val FOREGROUND_NOTIFICATION_ID = 1000
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Timber.d("PostingService started")
//        createNotificationChannel()
//        startForeground()
//
//        intent?.run {
//            if (hasExtra(EXTRA_POST)) {
//                val postParcel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    getParcelableExtra(EXTRA_POST, PostParcel::class.java)
//                } else {
//                    @Suppress("DEPRECATION")
//                    getParcelableExtra(EXTRA_POST)
//                }
//
//                postParcel?.run {
//                    lifecycleScope.launch(IO) {
//                        createPost(this@run)
//                    }
//                }
//            }
//        }
//
//        return super.onStartCommand(intent, flags, startId)
//    }
//
//    private fun createNotificationChannel() {
//        val channel = NotificationChannel(
//            CHANNEL_ID,
//            CHANNEL_NAME,
//            NotificationManager.IMPORTANCE_DEFAULT
//        )
//        channel.description = "백그라운드에서 게시물을 업로드합니다"
//        val notificationManager = getSystemService(NotificationManager::class.java)
//        notificationManager.createNotificationChannel(channel)
//    }
//
//    private fun startForeground() {
//        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_menu_upload)
//            .setContentTitle("게시물 업로드")
//            .setContentText("게시물을 업로드하는 중입니다...")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            startForeground(
//                FOREGROUND_NOTIFICATION_ID,
//                notification,
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
//            )
//        } else {
//            startForeground(
//                FOREGROUND_NOTIFICATION_ID,
//                notification
//            )
//        }
//    }
//
//    @SuppressLint("ObsoleteSdkInt")
//    private suspend fun createPost(postParcel: PostParcel) {
//        try {
//            val uploadedImages = postParcel.images.mapNotNull { imageParcel ->
//                val image = imageParcel.toImage()
//                when (val result = fileUseCase.uploadImage(image)) {
//                    is ApiResult.Success -> result.data
//                    is ApiResult.Error -> {
//                        Timber.e("Image upload failed: ${result.message}")
//                        return
//                    }
//                }
//            }
//
//            if (uploadedImages.size != postParcel.images.size) {
//                Timber.e("Some images failed to upload")
//                return
//            }
//
//            val contentParam = ContentParam(
//                text = postParcel.content,
//                images = uploadedImages
//            )
//
//            val postParam = PostParam(
//                title = postParcel.title,
//                content = contentParam.toJson()
//            )
//
//            val response = postService.createPost(postParam.toRequestBody())
//            if (response.result == "SUCCESS") {
//                sendBroadcast(Intent(ACTION_POSTED).apply {
//                    setPackage(packageName)
//                })
//            }
//        } catch (e: Exception) {
//            Timber.e(e, "Post creation failed")
//        } finally {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                stopForeground(STOP_FOREGROUND_REMOVE)
//            } else {
//                @Suppress("DEPRECATION")
//                stopForeground(true)
//            }
//            stopSelf()
//        }
//    }
//}