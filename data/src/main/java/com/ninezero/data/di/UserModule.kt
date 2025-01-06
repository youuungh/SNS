package com.ninezero.data.di

import com.ninezero.data.usecase.FeedUseCaseImpl
import com.ninezero.data.usecase.FileUseCaseImpl
import com.ninezero.data.usecase.PostUseCaseImpl
import com.ninezero.data.usecase.ThemeUseCaseImpl
import com.ninezero.data.usecase.UserUseCaseImpl
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.FileUseCase
import com.ninezero.domain.usecase.PostUseCase
import com.ninezero.domain.usecase.ThemeUseCase
import com.ninezero.domain.usecase.UserUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    @Binds
    abstract fun bindUserUseCase(userUseCaseImpl: UserUseCaseImpl): UserUseCase

    @Binds
    abstract fun bindFileUseCase(fileUseCaseImpl: FileUseCaseImpl): FileUseCase

    @Binds
    abstract fun bindPostUseCase(postUseCaseImpl: PostUseCaseImpl): PostUseCase

    @Binds
    abstract fun bindFeedUseCase(feedUseCaseImpl: FeedUseCaseImpl): FeedUseCase

    @Binds
    abstract fun bindThemeUseCase(themeUseCaseImpl: ThemeUseCaseImpl): ThemeUseCase
}