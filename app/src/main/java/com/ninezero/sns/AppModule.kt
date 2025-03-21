package com.ninezero.sns

import android.content.Context
import androidx.work.WorkManager
import com.ninezero.data.ktor.SocialAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWebClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }

    @Provides
    @Singleton
    fun provideSocialAuthManager(@ApplicationContext context: Context, webClientId: String): SocialAuthManager {
        return SocialAuthManager(context, webClientId)
    }
}