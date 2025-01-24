package com.ninezero.domain.repository

import androidx.paging.PagingData
import com.ninezero.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<PagingData<Post>>
}