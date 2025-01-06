package com.ninezero.data.usecase

import androidx.paging.PagingData
import com.ninezero.data.model.CommentParam
import com.ninezero.data.repository.PostRepository
import com.ninezero.data.retrofit.PostService
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import com.ninezero.domain.usecase.FeedUseCase
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class FeedUseCaseImpl @Inject constructor(
    private val postRepository: PostRepository,
    private val postService: PostService
) : FeedUseCase {

    override suspend fun getPosts(): ApiResult<Flow<PagingData<Post>>> = try {
        ApiResult.Success(postRepository.getPosts())
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("게시물을 불러오는데 실패했습니다")
    }

    override suspend fun postComment(
        postId: Long,
        text: String
    ): ApiResult<Long> = try {
        val requestBody = CommentParam(text).toRequestBody()
        val response = postService.postComment(postId = postId, requestBody = requestBody)
        ApiResult.Success(response.data)
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                ApiResult.Error.ServerError("댓글 작성에 실패했습니다")
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }

    override suspend fun deletePost(postId: Long): ApiResult<Long> = try {
        val response = postService.deletePost(id = postId)
        ApiResult.Success(response.data)
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                ApiResult.Error.ServerError("게시물 삭제에 실패했습니다")
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }

    override suspend fun deleteComment(
        postId: Long,
        commentId: Long
    ): ApiResult<Long> = try {
        val response = postService.deleteComment(postId, commentId)
        ApiResult.Success(response.data)
    } catch (e: Exception) {
        when (e) {
            is retrofit2.HttpException -> {
                Timber.e("HTTP Error: ${e.code()}")
                ApiResult.Error.ServerError("댓글 삭제에 실패했습니다")
            }
            else -> {
                Timber.e("Network Error: ${e.message}")
                ApiResult.Error.NetworkError("네트워크 오류가 발생했습니다")
            }
        }
    }
}