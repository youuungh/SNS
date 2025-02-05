package com.ninezero.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ninezero.data.db.post.mypost.MyPostRemoteMediator
import com.ninezero.data.db.post.PostDatabase
import com.ninezero.data.db.post.PostRemoteMediator
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.ktor.PostService
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val database: PostDatabase,
    private val postService: PostService,
    private val networkRepository: NetworkRepository
) : PostRepository {
    override fun getPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            remoteMediator = PostRemoteMediator(
                database = database,
                postService = postService,
                networkRepository = networkRepository
            ),
            pagingSourceFactory = { database.postDao().getAll() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override fun getMyPosts(): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                prefetchDistance = 2,
                enablePlaceholders = true
            ),
            remoteMediator = MyPostRemoteMediator(
                database = database,
                postService = postService,
                networkRepository = networkRepository
            ),
            pagingSourceFactory = { database.postDao().getMyAll() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}