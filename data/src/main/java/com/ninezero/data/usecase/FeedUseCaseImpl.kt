package com.ninezero.data.usecase

import androidx.paging.PagingData
import com.ninezero.data.model.param.CommentParam
import com.ninezero.data.ktor.PostService
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val networkRepository: NetworkRepository,
    private val userUseCase: UserUseCase
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
    ): ApiResult<Long> {
        return try {
            // 오프라인 상태 체크
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            // 현재 사용자 정보
            val currentUser = when (val userResult = userUseCase.getMyUser()) {
                is ApiResult.Success -> userResult.data
                is ApiResult.Error -> return ApiResult.Error.ServerError("사용자 정보를 가져올 수 없습니다")
            }

            val commentParam = CommentParam(text)
            val response = postService.addComment(postId = postId, requestBody = commentParam)

            if (response.result == "SUCCESS") {
                // 서버 응답 성공 시 로컬 DB도 업데이트
                postRepository.addComment(postId, Comment(
                    id = response.data!!,
                    text = text,
                    userName = currentUser.userName,
                    profileImageUrl = currentUser.profileImagePath
                ))
                ApiResult.Success(response.data)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "댓글 작성에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deletePost(postId: Long): ApiResult<Long> {
        return try {
            // 오프라인 상태 체크
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = postService.deletePost(id = postId)

            if (response.result == "SUCCESS") {
                // 서버 응답 성공 시 로컬 DB도 업데이트
                postRepository.deletePost(postId)
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
            // 오프라인 상태 체크
            if (!networkRepository.isNetworkAvailable()) {
                return ApiResult.Error.NetworkError("네트워크 연결 상태를 확인해주세요")
            }

            val response = postService.deleteComment(postId, commentId)

            if (response.result == "SUCCESS") {
                // 서버 응답 성공 시 로컬 DB도 업데이트
                postRepository.deleteComment(postId, commentId)
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "댓글 삭제에 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun synchronizeData() {
        val isOnline = networkRepository.observeNetworkConnection().first()
        if (isOnline) {
            postRepository.synchronizeWithServer()
        }
    }
}