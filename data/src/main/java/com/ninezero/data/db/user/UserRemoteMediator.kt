package com.ninezero.data.db.user

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ninezero.data.ktor.UserService
import com.ninezero.data.model.dto.UserDto
import com.ninezero.domain.repository.NetworkRepository
import kotlinx.coroutines.delay

class UserRemoteMediator(
    private val database: UserDatabase,
    private val userService: UserService,
    private val networkRepository: NetworkRepository
) : RemoteMediator<Int, UserDto>() {

    private val userDao = database.userDao()
    private val remoteKeyDao = database.userRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserDto>
    ): MediatorResult {
        try {
            if (!networkRepository.isNetworkAvailable()) {
                return MediatorResult.Success(endOfPaginationReached = false)
            }

            if (loadType == LoadType.APPEND) {
                delay(2000)
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

            val response = userService.getAllUsers(page = page, size = state.config.pageSize)
            val users = response.data ?: emptyList()
            val endOfPaginationReached = users.isEmpty()

            val prevPage = if (page == 1) null else page - 1
            val nextPage = if (endOfPaginationReached) null else page + 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteAll()
                    userDao.deleteAll()
                }

                val keys = users.map { user ->
                    UserRemoteKey(
                        id = user.id,
                        prevPage = prevPage,
                        nextPage = nextPage,
                    )
                }

                remoteKeyDao.insertAll(keys)
                userDao.insertAll(users)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, UserDto>): UserRemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let {
                database.withTransaction {
                    remoteKeyDao.getRemoteKeyById(it.id)
                }
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, UserDto>): UserRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let {
                database.withTransaction {
                    remoteKeyDao.getRemoteKeyById(it.id)
                }
            }
    }
}