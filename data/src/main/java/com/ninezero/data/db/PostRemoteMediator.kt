package com.ninezero.data.db

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninezero.data.model.PostDto
import com.ninezero.data.retrofit.PostService
import javax.inject.Inject

class PostRemoteMediator @Inject constructor(
    private val database: PostDatabase,
    private val service: PostService
) : RemoteMediator<Int, PostDto>() {

    private val remoteKeyDao = database.remoteKeyDao()
    private val postDao = database.postDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostDto>
    ): MediatorResult {
        val remoteKey = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(true)
            LoadType.APPEND -> remoteKeyDao.getNextKey()
        }

        try {
            val page = remoteKey?.nextPage ?: 1
            val loadSize = 10

            val response = service.getPosts(page = page, size = loadSize)
            val posts = response.data

            database.withTransaction {
                if (loadType == LoadType.REFRESH){
                    postDao.deleteAll()
                    remoteKeyDao.deleteAll()
                }
                remoteKeyDao.upsert(RemoteKey(nextPage = page + 1))
                postDao.insertAll(posts)
            }

            return MediatorResult.Success(loadSize != posts.size)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}