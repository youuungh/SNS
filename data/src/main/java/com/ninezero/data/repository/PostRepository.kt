package com.ninezero.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ninezero.data.db.PostDatabase
import com.ninezero.data.db.PostRemoteMediator
import com.ninezero.data.model.toDomain
import com.ninezero.data.retrofit.PostService
import com.ninezero.domain.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val database: PostDatabase,
    private val postService: PostService
) {
    companion object {
        private const val PAGE_SIZE = 10
    }

    fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = PAGE_SIZE
            ),
            remoteMediator = PostRemoteMediator(
                database = database,
                service = postService
            ),
            pagingSourceFactory = {
                database.postDao().getAll()
            }
        ).flow.map { pagingData ->
            pagingData.map { postDto -> postDto.toDomain() }
        }
    }
}