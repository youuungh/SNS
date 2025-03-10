package com.ninezero.data.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ninezero.data.db.post.paging.CommentPagingSource
import com.ninezero.data.db.post.paging.SavedPostPagingSource
import com.ninezero.data.db.post.paging.UserPostPagingSource
import com.ninezero.data.model.param.CommentParam
import com.ninezero.data.ktor.PostService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.model.param.UpdatePostParam
import com.ninezero.data.repository.PostRepositoryImpl.Companion.PAGE_SIZE
import com.ninezero.data.util.handleNetworkException
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.usecase.FeedUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
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

    override suspend fun getSavedPosts(): ApiResult<Flow<PagingData<Post>>> = try {
        ApiResult.Success(
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    prefetchDistance = 2
                )
            ) {
                SavedPostPagingSource(postService)
            }.flow
        )
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("게시물을 불러오는데 실패했습니다")
    }

    override suspend fun getPostsById(userId: Long): ApiResult<Flow<PagingData<Post>>> = try {
        ApiResult.Success(
            Pager(
                PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    prefetchDistance = 2
                )
            ) {
                UserPostPagingSource(postService, userId)
            }.flow
        )
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("게시물을 불러오는데 실패했습니다")
    }

    override suspend fun updatePost(
        postId: Long,
        content: String,
        images: List<String>
    ): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val updateContent = buildJsonObject {
                put("text", JsonPrimitive(content))
                putJsonArray("images") {
                    images.forEach { add(JsonPrimitive(it)) }
                }
            }.toString()

            val param = UpdatePostParam(title = "제목없음", content = updateContent)

            val response = postService.updatePost(postId, param)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "게시물 수정에 실패했습니다")
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

    override suspend fun getComments(postId: Long): ApiResult<Flow<PagingData<Comment>>> = try {
        ApiResult.Success(
            Pager(
                config = PagingConfig(
                    pageSize = COMMENT_PAGE_SIZE,
                    initialLoadSize = COMMENT_PAGE_SIZE,
                    prefetchDistance = 1
                )
            ) {
                CommentPagingSource(postService, postId)
            }.flow
        )
    } catch (e: Exception) {
        Timber.e("Network Error: ${e.message}")
        ApiResult.Error.NetworkError("댓글을 불러오는데 실패했습니다")
    }

    override suspend fun getReplies(postId: Long, parentId: Long): ApiResult<List<Comment>> {
        return try {
            checkNetwork()?.let { return it }

            val response = postService.getReplies(postId, parentId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data?.map { it.toDomain() } ?: emptyList())
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "답글을 불러오는데 실패했습니다")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun addComment(
        postId: Long,
        text: String,
        parentId: Long?,
        mentionedUserIds: List<Long>?,
        replyToCommentId: Long?
    ): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val commentParam = CommentParam(
                comment = text,
                parentId = parentId,
                mentionedUserIds = mentionedUserIds,
                replyToCommentId = replyToCommentId
            )
            val response = postService.addComment(postId = postId, requestBody = commentParam)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                val message = if (parentId != null) "답글 작성에 실패했습니다" else "댓글 작성에 실패했습니다"
                ApiResult.Error.ServerError(response.errorMessage ?: message)
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun deleteComment(postId: Long, commentId: Long): ApiResult<Long> {
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

    override suspend fun savePost(postId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.savePost(postId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "저장 실패")
            }
        } catch (e: Exception) {
            e.handleNetworkException()
        }
    }

    override suspend fun unsavePost(postId: Long): ApiResult<Long> {
        return try {
            checkNetwork()?.let { return it } // 네트워크 상태 확인

            val response = postService.unsavePost(postId)
            if (response.result == "SUCCESS") {
                ApiResult.Success(response.data!!)
            } else {
                ApiResult.Error.ServerError(response.errorMessage ?: "저장 취소 실패")
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

    companion object {
        const val COMMENT_PAGE_SIZE = 20
    }
}