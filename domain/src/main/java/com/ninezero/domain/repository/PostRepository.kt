package com.ninezero.domain.repository

import androidx.paging.PagingData
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<PagingData<Post>>
    suspend fun getPost(postId: Long): Post?
    suspend fun addComment(postId: Long, comment: Comment)
    suspend fun deleteComment(postId: Long, commentId: Long)
    suspend fun deletePost(postId: Long)
    suspend fun synchronizeWithServer()
}