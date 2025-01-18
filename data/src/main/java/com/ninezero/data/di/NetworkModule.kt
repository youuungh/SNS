package com.ninezero.data.di

import com.ninezero.data.ktor.FileService
import com.ninezero.data.ktor.PostService
import com.ninezero.data.ktor.TokenInterceptor
import com.ninezero.data.ktor.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton

const val BASE_URL = "http://10.0.2.2:8080/"

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    /** retrofit
    @Provides
    fun provideOkHttpClient(interceptor: TokenInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val converterFactory = Json {
            ignoreUnknownKeys = true
        }.asConverterFactory("application/json; charset=UTF8".toMediaType())

        return Retrofit.Builder()
            .baseUrl("${BASE_URL}api/")
            .addConverterFactory(converterFactory)
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideUserService(retrofit: Retrofit): UserService {
    return retrofit.create(UserService::class.java)
    }

    @Provides
    fun provideFileService(retrofit: Retrofit): FileService {
    return retrofit.create(FileService::class.java)
    }

    @Provides
    fun providePostService(retrofit: Retrofit): PostService {
    return retrofit.create(PostService::class.java)
    }
    */

    @Provides
    @Singleton
    fun provideHttpClient(tokenInterceptor: TokenInterceptor): HttpClient {
        return HttpClient(Android) {
            // JSON 직렬화
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            // 로깅
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d("Ktor: $message")
                    }
                }
                level = LogLevel.INFO
            }

            // 기본 요청
            install(DefaultRequest) {
                url("${BASE_URL}api/")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                val token = tokenInterceptor.getToken()
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }

            // 엔진
            engine {
                connectTimeout = 10_000
                socketTimeout = 15_000
            }
        }
    }

    @Provides
    @Singleton
    fun provideUserService(client: HttpClient): UserService {
        return UserService(client)
    }

    @Provides
    @Singleton
    fun provideFileService(client: HttpClient): FileService {
        return FileService(client)
    }

    @Provides
    @Singleton
    fun providePostService(client: HttpClient): PostService {
        return PostService(client)
    }
}