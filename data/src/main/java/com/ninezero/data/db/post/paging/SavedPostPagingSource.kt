package com.ninezero.data.db.post.paging

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.ninezero.data.ktor.PostService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.domain.model.Post
import javax.inject.Inject

class SavedPostPagingSource @Inject constructor(
    private val postService: PostService
) : PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        return try {
            val page = params.key ?: 1

            val response = postService.getSavedPosts(page, params.loadSize)
            val posts = response.data?.map { it.toDomain() } ?: emptyList()

            LoadResult.Page(
                data = posts,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (posts.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}