package com.ninezero.data.db.notification.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ninezero.data.ktor.NotificationService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.domain.model.Notification
import javax.inject.Inject

class NotificationPagingSource @Inject constructor(
    private val notificationService: NotificationService
) : PagingSource<Int, Notification>() {
    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notification> {
        return try {
            val page = params.key ?: 1
            val response = notificationService.getNotifications(
                page = page,
                size = params.loadSize
            )
            val notifications = response.data?.map { it.toDomain() } ?: emptyList()

            LoadResult.Page(
                data = notifications,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (notifications.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}