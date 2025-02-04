package com.ninezero.data.di

import com.ninezero.data.db.post.PostDatabase
import com.ninezero.data.db.user.UserDatabase
import com.ninezero.data.ktor.PostService
import com.ninezero.data.ktor.UserService
import com.ninezero.data.network.NetworkObserver
import com.ninezero.data.repository.NetworkRepositoryImpl
import com.ninezero.data.repository.PostRepositoryImpl
import com.ninezero.data.repository.UserRepositoryImpl
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideNetworkRepository(networkObserver: NetworkObserver): NetworkRepository {
        return NetworkRepositoryImpl(networkObserver)
    }

    @Provides
    @Singleton
    fun providePostRepository(
        database: PostDatabase,
        postService: PostService,
        networkRepository: NetworkRepository
    ): PostRepository {
        return PostRepositoryImpl(
            database = database,
            postService = postService,
            networkRepository = networkRepository
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        database: UserDatabase,
        userService: UserService,
        networkRepository: NetworkRepository
    ) : UserRepository {
        return UserRepositoryImpl(
            database = database,
            userService = userService,
            networkRepository = networkRepository
        )
    }
}