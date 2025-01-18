package com.ninezero.data.db

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninezero.data.model.dto.PostDto
import com.ninezero.data.ktor.PostService
import com.ninezero.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class PostRemoteMediator @Inject constructor(
    private val database: PostDatabase,
    private val service: PostService,
    private val networkRepository: NetworkRepository
) : RemoteMediator<Int, PostDto>() {

    private val remoteKeyDao = database.remoteKeyDao()
    private val postDao = database.postDao()

    override suspend fun initialize(): InitializeAction {
        // 초기 로드 시 로컬 DB에 데이터가 있으면 새로고침 스킵
        return if (postDao.getCount() > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostDto>
    ): MediatorResult {
        try {
            // 오프라인이고 새로고침이 아닌 경우 추가 로드 중단
            val isOnline = networkRepository.observeNetworkConnection().first()
            if (!isOnline && loadType != LoadType.REFRESH) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            val page = when (loadType) {
                LoadType.REFRESH -> {
                    // 새로고침 시에는 동기화 후 첫 페이지만 로드
                    synchronizeWithServer(state.config.pageSize)
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> remoteKeyDao.getNextKey().nextPage
            }

            val response = service.getPosts(page = page, size = state.config.pageSize)
            val posts = response.data ?: emptyList()

            // DB 업데이트
            database.withTransaction {
                remoteKeyDao.upsert(RemoteKey(nextPage = page + 1))
                postDao.insertAll(posts)
            }

            return MediatorResult.Success(endOfPaginationReached = posts.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun synchronizeWithServer(pageSize: Int) {
        try {
            // 서버의 첫 페이지 데이터 가져오기
            val response = service.getPosts(page = 1, size = pageSize)
            val serverPosts = response.data ?: emptyList()
            val serverPostIds = serverPosts.map { it.id }.toSet()

            // 현재 로컬 DB의 모든 게시물 ID 가져오기
            val localPostIds = postDao.getAllPostIds().toSet()

            // 서버에 없는 게시물 찾기
            val deletedPostIds = localPostIds - serverPostIds

            database.withTransaction {
                // 서버에 없는 게시물 삭제
                deletedPostIds.forEach { postId ->
                    postDao.deleteById(postId)
                }

                // 새로운 데이터 저장
                postDao.insertAll(serverPosts)
                remoteKeyDao.deleteAll()
                remoteKeyDao.upsert(RemoteKey(nextPage = 2))
            }
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }
}