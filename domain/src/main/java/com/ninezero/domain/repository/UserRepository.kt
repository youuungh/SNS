package com.ninezero.domain.repository

import androidx.paging.PagingData
import com.ninezero.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<PagingData<User>>
    suspend fun getMyUser(): User?
    suspend fun updateMyUser(user: User)
}