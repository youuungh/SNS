package com.ninezero.presentation.setting

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.ninezero.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : BaseActivity() {
    @Composable
    override fun Content() {
        SettingScreen(
            onNavigateToBack = { finish() }
        )
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, SettingActivity::class.java)
    }
}