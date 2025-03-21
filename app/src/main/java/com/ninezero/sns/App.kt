package com.ninezero.sns

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import com.ninezero.domain.usecase.AuthUseCase
import com.ninezero.domain.usecase.FCMUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var fcmUseCase: FCMUseCase

    @Inject
    lateinit var authUseCase: AuthUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        NaverIdLoginSDK.initialize(
            this,
            BuildConfig.NAVER_CLIENT_ID,
            BuildConfig.NAVER_CLIENT_SECRET,
            "SNS"
        )
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        initializeFCM()
    }

    private fun initializeFCM() {
        appScope.launch {
            try {
                authUseCase.getToken()?.let {
                    val token = FirebaseMessaging.getInstance().token.await()
                    fcmUseCase.registerToken(token)
                }
            } catch (e: Exception) {
                Timber.e(e, "FCM 토큰 초기화 실패")
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()
}