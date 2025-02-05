package com.ninezero.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.ninezero.data.db.user.UserDatabase
import com.ninezero.data.db.user.UserRemoteMediator
import com.ninezero.data.ktor.UserService
import com.ninezero.data.model.dto.toDomain
import com.ninezero.data.model.dto.toDto
import com.ninezero.domain.model.User
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val database: UserDatabase,
    private val userService: UserService,
    private val networkRepository: NetworkRepository
) : UserRepository {
    private val myUserDao = database.myUserDao()

    override fun getAllUsers(): Flow<PagingData<User>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = 1,
                enablePlaceholders = true
            ),
            remoteMediator = UserRemoteMediator(
                database = database,
                userService = userService,
                networkRepository = networkRepository
            ),
            pagingSourceFactory = { database.userDao().getAll() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getMyUser(): User? {
        return myUserDao.getMyUser()?.toDomain()
    }

    override suspend fun updateMyUser(user: User) {
        myUserDao.insert(user.toDto())
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}