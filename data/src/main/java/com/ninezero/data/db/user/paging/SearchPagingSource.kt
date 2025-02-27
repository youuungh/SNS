package com.ninezero.data.db.user.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ninezero.data.ktor.UserService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.domain.model.User
import javax.inject.Inject

class SearchPagingSource @Inject constructor(
    private val userService: UserService,
    private val query: String
) : PagingSource<Int, User>() {
    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return try {
            val page = params.key ?: 1
            val response = userService.searchUsers(
                query = query,
                page = page,
                size = params.loadSize
            )
            val users = response.data?.map { it.toDomain() } ?: emptyList()

            LoadResult.Page(
                data = users,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (users.size < params.loadSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}