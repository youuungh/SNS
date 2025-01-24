package com.ninezero.presentation.main

import android.content.Intent
import androidx.compose.runtime.Composable
import com.ninezero.presentation.auth.AuthActivity
import com.ninezero.presentation.base.BaseActivity
import com.ninezero.presentation.post.PostActivity
import com.ninezero.presentation.setting.SettingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Composable
    override fun Content() {
        MainScreen(
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