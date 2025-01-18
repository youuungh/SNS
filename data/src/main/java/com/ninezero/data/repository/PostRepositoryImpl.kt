package com.ninezero.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.ninezero.data.db.PostDatabase
import com.ninezero.data.db.PostRemoteMediator
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.ktor.PostService
import com.ninezero.data.model.dto.CommentDto
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import com.ninezero.domain.usecase.UserUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val database: PostDatabase,
    private val postService: PostService,
    private val networkRepository: NetworkRepository,
    private val userUseCase: UserUseCase
) : PostRepository  {
    companion object {
        private const val PAGE_SIZE = 10
    }

    override fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = PAGE_SIZE
            ),
            // 서버-로컬 DB 동기화
            remoteMediator = PostRemoteMediator(
                database = database,
                service = postService,
                networkRepository = networkRepository
            ),
            pagingSourceFactory = {
                database.postDao().getAll()
            }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getPost(postId: Long): Post? {
        return database.postDao().getPostById(postId)?.toDomain()
    }

    // 로컬 DB 업데이트
    override suspend fun addComment(postId: Long, comment: Comment) {
        val currentUser = when (val result = userUseCase.getMyUser()) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> return
        }

        database.postDao().getPostById(postId)?.let { postDto ->
            val newComment = CommentDto(
                id = comment.id,
                comment = comment.text,
                createdAt = System.currentTimeMillis().toString(),
                createUserId = currentUser.id,
                createUserName = comment.userName,
                profileImageUrl = comment.profileImageUrl ?: ""
            )
            val updatedPost = postDto.copy(
                comments = postDto.comments + newComment
            )
            database.postDao().updatePost(updatedPost)
        }
    }

    override suspend fun deletePost(postId: Long) {
        database.postDao().deleteById(postId)
    }

    override suspend fun deleteComment(postId: Long, commentId: Long) {
        database.postDao().getPostById(postId)?.let { postDto ->
            val updatedPost = postDto.copy(
                comments = postDto.comments.filterNot { it.id == commentId }
            )
            database.postDao().updatePost(updatedPost)
        }
    }

    // 서버와 로컬 DB 동기화
    override suspend fun synchronizeWithServer() {
        try {
            // 현재 로컬 DB의 게시물 ID 가져오기
            val localPostIds = database.postDao().getAllPostIds().toSet()

            // 서버의 최신 데이터 가져오기
            val response = postService.getPosts(page = 1, size = PAGE_SIZE)
            val serverPosts = response.data ?: emptyList()
            val serverPostIds = serverPosts.map { it.id }.toSet()

            // 서버에 없는 게시물 찾기
            val deletedPostIds = localPostIds - serverPostIds

            // DB 업데이트
            database.withTransaction {

                // 서버에서 삭제된 게시물 제거
                deletedPostIds.forEach { postId ->
                    database.postDao().deleteById(postId)
                }
                // 새로운 데이터 저장
                database.postDao().insertAll(serverPosts)
            }
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }
}