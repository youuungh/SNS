package com.ninezero.data.usecase

import androidx.paging.PagingData
import com.ninezero.data.model.param.CommentParam
import com.ninezero.data.ktor.PostService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.usecase.FeedUseCase
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

/** retrofit
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

    override suspend fun addComment(
        postId: Long,
        text: String
    ): ApiResult<Long> = try {
        val requestBody = CommentParam(text).toRequestBody()
        val response = postService.addComment(postId = postId, requestBody = requestBody)
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
*/

class FeedUseCaseImpl @Inject constructor(
    private val postService: PostService,
    private val postRepository: PostRepository,
    private val networkRepository: NetworkRepository
) : FeedUseCase {
    override suspend fun getPosts(): ApiResult<Flow<PagingData<Post>>> = try {
        ApiResult.Success(postRepository.getPosts())
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("게시물을 불러오는데 실패했습니다")
    }

    override suspend fun getMyPosts(): ApiResult<Flow<PagingData<Post>>> = try {
        ApiResult.Success(postRepository.getMyPosts())
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("게시물을 불러오는데 실패했습니다")
    }

    override suspend fun addComment(
        postId: Long,
        text: String
    ): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val commentParam = CommentParam(text)
            val response = postService.addComment(postId = postId, requestBody = commentParam)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "댓글 작성에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deletePost(postId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.deletePost(id = postId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "게시물 삭제에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deleteComment(
        postId: Long,
        commentId: Long
    ): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.deleteComment(postId, commentId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "댓글 삭제에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun likePost(postId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.likePost(postId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "좋아요 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun unlikePost(postId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.unlikePost(postId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "좋아요 취소 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    private suspend fun checkNetwork(): ApiResult.Error.NetworkError? {
        return if (!networkRepository.isNetworkAvailable()) {
            ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
        } else {
            null
        }
    }
}