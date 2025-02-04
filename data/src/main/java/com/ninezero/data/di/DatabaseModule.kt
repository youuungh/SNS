package com.ninezero.data.di

import android.content.Context
import androidx.room.Room
import com.ninezero.data.db.post.PostDao
import com.ninezero.data.db.post.PostDatabase
import com.ninezero.data.db.post.PostRemoteKeyDao
import com.ninezero.data.db.user.UserDao
import com.ninezero.data.db.user.UserDatabase
import com.ninezero.data.db.user.UserRemoteKeyDao
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
            "post_db"
        ).build()

    @Provides
    @Singleton
    fun provideUserDatabase(
        @ApplicationContext context: Context
    ): UserDatabase =
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_db"
        ).build()

    @Provides
    @Singleton
    fun providePostDao(database: PostDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun providePostRemoteKeyDao(database: PostDatabase): PostRemoteKeyDao {
        return database.postRemoteKeyDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: UserDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideUserRemoteKeyDao(database: UserDatabase): UserRemoteKeyDao {
        return database.userRemoteKeyDao()
    }
}