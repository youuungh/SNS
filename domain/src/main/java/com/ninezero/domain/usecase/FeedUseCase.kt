package com.ninezero.domain.usecase

import androidx.paging.PagingData
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedUseCase {
    suspend fun getPosts(): ApiResult<Flow<PagingData<Post>>>
    suspend fun getMyPosts(): ApiResult<Flow<PagingData<Post>>>
    suspend fun addComment(postId: Long, text: String): ApiResult<Long>
    suspend fun updatePost(postId: Long, content: String, images: List<String>): ApiResult<Long>
    suspend fun deletePost(postId: Long): ApiResult<Long>
    suspend fun deleteComment(postId: Long, commentId: Long): ApiResult<Long>
    suspend fun likePost(postId: Long): ApiResult<Long>
    suspend fun unlikePost(postId: Long): ApiResult<Long>
}