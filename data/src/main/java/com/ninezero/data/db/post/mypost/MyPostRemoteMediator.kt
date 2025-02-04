package com.ninezero.data.db.post.mypost

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninezero.data.db.post.PostDatabase
import com.ninezero.data.ktor.PostService
import com.ninezero.data.model.dto.PostDto
import com.ninezero.domain.repository.NetworkRepository
import javax.inject.Inject

class MyPostRemoteMediator @Inject constructor(
    private val database: PostDatabase,
    private val postService: PostService,
    private val networkRepository: NetworkRepository
) : RemoteMediator<Int, PostDto>() {
    private val postDao = database.postDao()
    private val remoteKeyDao = database.myPostRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostDto>
    ): MediatorResult {
        try {
            if (!networkRepository.isNetworkAvailable()) {
                return MediatorResult.Success(endOfPaginationReached = false)
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
                    val nextPage = remoteKey?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    nextPage
                }
            }

            val response = postService.getMyPosts(page = page, size = state.config.pageSize)
            val myPosts = response.data ?: emptyList()
            val endOfPaginationReached = myPosts.isEmpty()

            val prevPage = if (page == 1) null else page - 1
            val nextPage = if (endOfPaginationReached) null else page + 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteAll()
                    postDao.deleteMyAll()
                }

                val keys = myPosts.map { post ->
                    MyPostRemoteKey(
                        id = post.id,
                        prevPage = prevPage,
                        nextPage = nextPage,
                    )
                }

                remoteKeyDao.insertAll(remoteKeys = keys)
                postDao.insertMyAll(myPosts = myPosts)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, PostDto>
    ): MyPostRemoteKey? {
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
    ): MyPostRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let {
                database.withTransaction {
                    remoteKeyDao.getRemoteKeyById(it.id)
                }
            }
    }
}