package com.ninezero.data.db.post.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ninezero.data.ktor.PostService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.domain.model.Comment
import javax.inject.Inject

class CommentPagingSource @Inject constructor(
    private val postService: PostService,
    private val postId: Long
) : PagingSource<Int, Comment>() {
    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        return try {
            val page = params.key ?: 1
            val response = postService.getComments(
                postId = postId,
                page = page,
                size = params.loadSize
            )
            val comments = response.data?.map { it.toDomain() } ?: emptyList()

            LoadResult.Page(
                data = comments,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (comments.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}