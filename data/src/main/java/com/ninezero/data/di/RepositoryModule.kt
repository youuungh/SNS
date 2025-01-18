package com.ninezero.data.di

import com.ninezero.data.db.PostDatabase
import com.ninezero.data.ktor.PostService
import com.ninezero.data.network.NetworkObserver
import com.ninezero.data.repository.NetworkRepositoryImpl
import com.ninezero.data.repository.PostRepositoryImpl
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.usecase.UserUseCase
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
        networkRepository: NetworkRepository,
        userUseCase: UserUseCase
    ): PostRepository {
        return PostRepositoryImpl(
            database = database,
            postService = postService,
            networkRepository = networkRepository,
            userUseCase = userUseCase
        )
    }
}