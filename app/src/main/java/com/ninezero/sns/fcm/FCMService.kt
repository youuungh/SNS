package com.ninezero.sns.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ninezero.data.UserDataStore
import com.ninezero.domain.usecase.FCMTokenUseCase
import com.ninezero.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.ninezero.presentation.R
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenUseCase: FCMTokenUseCase

    @Inject
    lateinit var userDataStore: UserDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("새 FCM 토큰: $token")

        scope.launch {
            try {
                userDataStore.setFcmToken(token)
                userDataStore.getToken()?.let { fcmTokenUseCase.registerToken(token) }
            } catch (e: Exception) {
                Timber.e(e, "FCM 토큰 업데이트 실패")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM 메시지 수신: ${message.data}")

        val data = message.data
        val type = data["type"] ?: return
        val title = data["title"] ?: "알림"
        val body = data["body"] ?: "새로운 알림이 있습니다"

        val senderId = when (type) {
            "follow" -> data["userId"]?.toLongOrNull()
            else -> data["senderId"]?.toLongOrNull()
        }

        val boardId = data["boardId"]?.toLongOrNull()
        val commentId = data["commentId"]?.toLongOrNull()
        val roomId = data["roomId"]
        val senderLoginId = data["senderLoginId"]
        val senderName = data["senderName"]
        val senderProfileImagePath = data["senderProfileImagePath"]

        scope.launch {
            val myUserId = try {
                userDataStore.getUserId()
            } catch (e: Exception) {
                Timber.e(e, "사용자 ID 가져오기 실패")
                -1L
            }

            val intent = createNotificationIntent(
                type = type,
                senderId = senderId,
                boardId = boardId,
                commentId = commentId,
                roomId = roomId,
                senderLoginId = senderLoginId,
                senderName = senderName,
                senderProfileImagePath = senderProfileImagePath,
                myUserId = myUserId
            )

            withContext(Dispatchers.Main) {
                showNotification(title, body, intent)
            }
        }
    }

    private fun createNotificationIntent(
        type: String,
        senderId: Long?,
        boardId: Long?,
        commentId: Long?,
        roomId: String?,
        senderLoginId: String?,
        senderName: String?,
        senderProfileImagePath: String?,
        myUserId: Long
    ): Intent {
        return Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            putExtras(bundleOf(
                "notification_type" to type,
                "notification_sender_id" to (senderId ?: -1L),
                "notification_board_id" to (boardId ?: -1L),
                "notification_comment_id" to (commentId ?: -1L),
                "notification_room_id" to roomId,
                "notification_sender_login_id" to senderLoginId,
                "notification_sender_name" to senderName,
                "notification_sender_profile_image_path" to senderProfileImagePath,
                "notification_my_user_id" to myUserId
            ))

            createDeepLinkUri(type, senderId, boardId, commentId, myUserId)?.let {
                putExtra("deep_link_uri", it.toString())
            }
        }
    }

    private fun createDeepLinkUri(
        type: String,
        senderId: Long?,
        boardId: Long?,
        commentId: Long?,
        myUserId: Long
    ): Uri? = when (type) {
        "like", "comment" -> boardId?.let {
            val showComments = type == "comment"
            val commentParam = commentId?.let { "&commentId=$it" } ?: ""
            Uri.parse("ninezero://post_detail/$myUserId/$it?showComments=$showComments$commentParam")
        }
        "follow" -> senderId?.let { Uri.parse("ninezero://user/$it") }
        else -> null
    }

    private fun createNotificationChannel(channelId: String, notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_description)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String, intent: Intent) {
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationId = System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(channelId, notificationManager)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        Timber.d("알림 생성: ID=$notificationId, 제목=$title, 내용=$body")
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}