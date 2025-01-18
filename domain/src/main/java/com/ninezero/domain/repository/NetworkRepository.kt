package com.ninezero.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun observeNetworkConnection(): Flow<Boolean>
    suspend fun isNetworkAvailable(): Boolean
}