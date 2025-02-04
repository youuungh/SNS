package com.ninezero.data.db.post.mypost

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyPostRemoteKeyDao {
    @Query("SELECT * FROM MyPostRemoteKey WHERE id = :id")
    suspend fun getRemoteKeyById(id: Long): MyPostRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<MyPostRemoteKey>)

    @Query("DELETE FROM mypostremotekey")
    suspend fun deleteAll()
}