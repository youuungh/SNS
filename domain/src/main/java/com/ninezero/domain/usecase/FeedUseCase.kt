package com.ninezero.domain.usecase

import androidx.paging.PagingData
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedUseCase {
    suspend fun getPosts(): ApiResult<Flow<PagingData<Post>>>
    suspend fun getMyPosts(): ApiResult<Flow<PagingData<Post>>>
    suspend fun getSavedPosts(): ApiResult<Flow<PagingData<Post>>>
    suspend fun getPostsById(userId: Long): ApiResult<Flow<PagingData<Post>>>
    suspend fun updatePost(postId: Long, content: String, images: List<String>): ApiResult<Long>
    suspend fun deletePost(postId: Long): ApiResult<Long>

    // comment
    suspend fun getComments(postId: Long): ApiResult<Flow<PagingData<Comment>>>
    suspend fun getReplies(postId: Long, parentId: Long): ApiResult<List<Comment>>
    suspend fun addComment(postId: Long, text: String, parentId: Long?): ApiResult<Long>
    suspend fun deleteComment(postId: Long, commentId: Long): ApiResult<Long>

    // like & save
    suspend fun likePost(postId: Long): ApiResult<Long>
    suspend fun unlikePost(postId: Long): ApiResult<Long>
    suspend fun savePost(postId: Long): ApiResult<Long>
    suspend fun unsavePost(postId: Long): ApiResult<Long>
}