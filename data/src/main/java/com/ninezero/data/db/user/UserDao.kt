package com.ninezero.data.db.user

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninezero.data.model.dto.UserDto

@Dao
interface UserDao {
    @Query("SELECT * FROM userdto ORDER BY id DESC")
    fun getAll(): PagingSource<Int, UserDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserDto>)

    @Query("DELETE FROM userdto")
    suspend fun deleteAll()
}