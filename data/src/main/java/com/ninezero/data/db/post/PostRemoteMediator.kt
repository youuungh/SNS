package com.ninezero.data.db.post

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninezero.data.model.dto.PostDto
import com.ninezero.data.ktor.PostService
import com.ninezero.domain.repository.NetworkRepository
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

class PostRemoteMediator @Inject constructor(
    private val database: PostDatabase,
    private val postService: PostService,
    private val networkRepository: NetworkRepository
) : RemoteMediator<Int, PostDto>() {
    private val postDao = database.postDao()
    private val remoteKeyDao = database.postRemoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostDto>
    ): MediatorResult {
        Timber.d("LoadType: $loadType")
        try {
            if (!networkRepository.isNetworkAvailable()) {
                return MediatorResult.Success(endOfPaginationReached = false)
            }

            if (loadType == LoadType.APPEND) {
                Timber.d("APPEND delay")
                delay(1000)
            }

            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKey?.nextPage?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)
                    Timber.d("APPEND RemoteKey: $remoteKey")
                    val nextPage = remoteKey?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    Timber.d("APPEND nextPage: $nextPage")
                    nextPage
                }
            }

            val response = postService.getPosts(page = page, size = state.config.pageSize)
            val posts = (response.data ?: emptyList()).map { it.copy(isMyPost = false) }
            Timber.d("Server response for page $page: total posts=${posts.size}")
            val endOfPaginationReached = posts.isEmpty()

            val prevPage = if (page == 1) null else page - 1
            val nextPage = if (endOfPaginationReached) null else page + 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteAll()
                    postDao.deleteAll()
                }

                // RemoteKey 생성 및 저장
                val keys = posts.map { post ->
                    PostRemoteKey(
                        id = post.id,
                        prevPage = prevPage,
                        nextPage = nextPage,
                    )
                }

                remoteKeyDao.insertAll(remoteKeys = keys)
                postDao.insertAll(posts = posts)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, PostDto>
    ): PostRemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let {
                database.withTransaction {
                    remoteKeyDao.getRemoteKeyById(it.id)
                }
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(
        state: PagingState<Int, PostDto>
    ): PostRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let {
                database.withTransaction {
                    remoteKeyDao.getRemoteKeyById(it.id)
                }
            }
    }
}