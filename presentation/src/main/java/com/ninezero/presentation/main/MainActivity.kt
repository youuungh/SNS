package com.ninezero.presentation.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.compose.runtime.Composable
import com.ninezero.domain.usecase.UserUseCase
import com.ninezero.presentation.auth.AuthActivity
import com.ninezero.presentation.base.BaseActivity
import com.ninezero.presentation.post.PostActivity
import com.ninezero.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private var deepLink: Uri? = null
    private var chatNotificationData: ChatNotificationData? = null

    @Inject
    lateinit var userUseCase: UserUseCase

    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { handleFcmIntent(it) }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                fcmReceiver,
                IntentFilter("com.ninezero.FCM_RECEIVED"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(fcmReceiver, IntentFilter("com.ninezero.FCM_RECEIVED"))
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(fcmReceiver)
        } catch (e: Exception) {
            Timber.e(e, "FCM 리시버 등록 해제 실패")
        }
    }

    private fun handleIntent(intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type")
        if (notificationType != null) {
            Timber.d("FCM 알림 처리: $notificationType")

            when (notificationType) {
                "like", "comment" -> handlePostNotification(intent, notificationType)
                "follow" -> handleFollowNotification(intent)
                "chat" -> handleChatNotification(intent)
            }

            intent.getStringExtra("deep_link_uri")?.let {
                Timber.d("FCM 알림에서 딥링크 처리: $it")
                deepLink = Uri.parse(it)
            }

            return
        }

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            Timber.d("일반 딥링크 처리: ${intent.data}")
            deepLink = intent.data
            chatNotificationData = null
        }
    }

    private fun handleFcmIntent(intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type") ?: return
        Timber.d("FCM 브로드캐스트 처리: $notificationType")

        when (notificationType) {
            "like", "comment" -> handlePostNotification(intent, notificationType)
            "follow" -> handleFollowNotification(intent)
            "chat" -> handleChatNotification(intent)
        }

        intent.getStringExtra("deep_link_uri")?.let {
            deepLink = Uri.parse(it)
        }
    }

    private fun handlePostNotification(intent: Intent, type: String) {
        val boardId = intent.getLongExtra("notification_board_id", -1L)
        if (boardId == -1L) return

        val myUserId = intent.getLongExtra("notification_my_user_id", -1L).let {
            if (it != -1L) it else runBlocking { userUseCase.getMyUserId() }
        }

        val commentId = intent.getLongExtra("notification_comment_id", -1L)
        val commentParam = if (commentId != -1L) "&commentId=$commentId" else ""
        val showComments = type == "comment"

        deepLink = Uri.parse("ninezero://post_detail/$myUserId/$boardId?showComments=$showComments$commentParam")
        chatNotificationData = null
        Timber.d("게시물 알림 처리 완료: type=$type, myUserId=$myUserId, boardId=$boardId, commentId=$commentId")
    }

    private fun handleChatNotification(intent: Intent) {
        val senderId = intent.getLongExtra("notification_sender_id", -1L)
        val roomId = intent.getStringExtra("notification_room_id")
        val senderLoginId = intent.getStringExtra("notification_sender_login_id")
        val senderName = intent.getStringExtra("notification_sender_name")
        val senderProfilePath = intent.getStringExtra("notification_sender_profile_image_path")
        val myUserId = intent.getLongExtra("notification_my_user_id", -1L).let {
            if (it != -1L) it else runBlocking { userUseCase.getMyUserId() }
        }

        if (senderId != -1L && senderLoginId != null && senderName != null) {
            chatNotificationData = ChatNotificationData(
                otherUserId = senderId,
                roomId = roomId,
                otherUserLoginId = senderLoginId,
                otherUserName = senderName,
                otherUserProfilePath = senderProfilePath,
                myUserId = myUserId
            )
            deepLink = null
            Timber.d("채팅 알림 처리 완료: senderId=$senderId, roomId=$roomId")
        }
    }

    private fun handleFollowNotification(intent: Intent) {
        val senderId = intent.getLongExtra("notification_sender_id", -1L)
        if (senderId == -1L) return

        val uri = Uri.parse("ninezero://user/$senderId")
        deepLink = uri
        chatNotificationData = null
        Timber.d("팔로우 알림 처리 완료: senderId=$senderId, 딥링크=$uri")
    }

    @Composable
    override fun Content() {
        MainScreen(
            deepLink = deepLink,
            chatNotificationData = chatNotificationData,
            onNavigateToPost = {
                startActivity(PostActivity.createIntent(this))
            },
            onNavigateToSettings = {
                startActivity(SettingActivity.createIntent(this))
            },
            onNavigateToLogin = {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        )
    }
}

data class ChatNotificationData(
    val otherUserId: Long,
    val roomId: String?,
    val otherUserLoginId: String,
    val otherUserName: String,
    val otherUserProfilePath: String?,
    val myUserId: Long
)