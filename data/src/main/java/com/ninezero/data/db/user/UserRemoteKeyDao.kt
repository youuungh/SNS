package com.ninezero.data.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserRemoteKeyDao {
    @Query("SELECT * FROM userremotekey WHERE id = :id")
    suspend fun getRemoteKeyById(id: Long): UserRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<UserRemoteKey>)

    @Query("DELETE FROM userremotekey")
    suspend fun deleteAll()
}