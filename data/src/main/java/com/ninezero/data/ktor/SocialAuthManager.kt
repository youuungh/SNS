package com.ninezero.data.ktor

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class SocialAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webClientId: String
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    
    suspend fun getGoogleIdToken(): String {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setNonce(null)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(request = request, context = context)

            return handleGetCredentialResult(result)
        } catch (e: GetCredentialException) {
            Timber.e(e, "Credential Manager 오류: ${e.type}")
            throw IllegalStateException("Google 로그인 실패: ${e.message}")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getKakaoIdToken(): String = suspendCancellableCoroutine { continuation ->
        val callback = { token: OAuthToken?, error: Throwable? ->
            if (error != null) {
                continuation.resumeWithException(IllegalStateException("카카오 로그인 실패: ${error.message}"))
            } else if (token != null) {
                continuation.resumeWith(Result.success(token.accessToken))
            } else {
                continuation.resumeWithException(IllegalStateException("카카오 토큰이 null입니다"))
            }
        }

        // 카카오톡 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 로그인
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            // 카카오 계정 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    private fun handleGetCredentialResult(result: GetCredentialResponse): String {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                return googleIdTokenCredential.idToken
            } catch (e: GoogleIdTokenParsingException) {
                Timber.e(e, "Google ID Token 파싱 오류")
                throw IllegalStateException("Google ID Token 파싱 실패")
            }
        } else {
            throw IllegalStateException("Received an invalid credential type")
        }
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        return authResult.user?.getIdToken(false)?.await()?.token
            ?: throw IllegalStateException("Firebase ID Token 획득 실패")
    }

    fun signOut(loginType: String) {
        try {
            when (loginType) {
                "google" -> auth.signOut()
                "naver" -> NaverIdLoginSDK.logout()
                "kakao" -> UserApiClient.instance.logout { error ->
                    if (error != null) Timber.e(error, "카카오 로그아웃 실패")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "소셜 로그인($loginType) 로그아웃 실패")
        }
    }
}