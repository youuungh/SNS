package com.ninezero.data.di

import android.content.Context
import androidx.room.Room
import com.ninezero.data.db.PostDao
import com.ninezero.data.db.PostDatabase
import com.ninezero.data.db.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePostDatabase(
        @ApplicationContext context: Context
    ): PostDatabase =
        Room.databaseBuilder(
            context,
            PostDatabase::class.java,
            "post_database"
        ).build()

    @Provides
    @Singleton
    fun providePostDao(database: PostDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun provideRemoteKeyDao(database: PostDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }
}