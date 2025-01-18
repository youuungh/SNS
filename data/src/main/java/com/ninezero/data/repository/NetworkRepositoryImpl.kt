package com.ninezero.data.repository

import com.ninezero.data.network.NetworkObserver
import com.ninezero.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(
    private val networkObserver: NetworkObserver
) : NetworkRepository {
    override fun observeNetworkConnection(): Flow<Boolean> =
        networkObserver.observe()

    override suspend fun isNetworkAvailable(): Boolean {
        return networkObserver.getCurrentState()
    }
}