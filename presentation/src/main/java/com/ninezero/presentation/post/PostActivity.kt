package com.ninezero.presentation.post

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.ninezero.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostActivity : BaseActivity() {
    @Composable
    override fun Content() {
        PostNavHost { finish() }
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, PostActivity::class.java)
    }
}