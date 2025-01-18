package com.ninezero.data.ktor

import com.ninezero.data.UserDataStore
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

/** retrofit
class TokenInterceptor @Inject constructor(
    private val userDatastore: UserDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token: String? = runBlocking { userDatastore.getToken() }
        return chain.proceed(
            chain.request()
                .newBuilder()
                .run {
                    if (token.isNullOrEmpty()) {
                        this
                    } else {
                        addHeader("Token", token)
                    }
                }
                .addHeader("Content-Type", "application/json; charset=UTF8")
                .build()
        )
    }
}
*/

class TokenInterceptor @Inject constructor(
    private val userDatastore: UserDataStore
) {
    fun getToken(): String? = runBlocking {
        val token = userDatastore.getToken()
        Timber.d("Token retrieved in TokenInterceptor: $token")
        token
    }
}