package com.ninezero.presentation.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.ninezero.domain.model.ACTION_POSTED
import com.ninezero.presentation.auth.AuthActivity
import com.ninezero.presentation.base.BaseActivity
import com.ninezero.presentation.feed.FeedViewModel
import com.ninezero.presentation.post.PostActivity
import com.ninezero.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val feedViewModel: FeedViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_POSTED) {
                feedViewModel.reload()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ACTION_POSTED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    @Composable
    override fun Content() {
        MainScreen(
            feedViewModel = feedViewModel,
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}