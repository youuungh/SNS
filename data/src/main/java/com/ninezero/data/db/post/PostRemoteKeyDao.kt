package com.ninezero.data.db.post

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT * FROM postremotekey WHERE id = :id")
    suspend fun getRemoteKeyById(id: Long): PostRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<PostRemoteKey>)

    @Query("DELETE FROM postremotekey")
    suspend fun deleteAll()
}