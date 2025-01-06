package com.ninezero.domain.usecase

import com.ninezero.domain.model.Image

interface PostUseCase {
    suspend fun getImageList(): List<Image>
    suspend fun createPost(title: String, content: String, images: List<Image>)
}