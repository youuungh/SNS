package com.ninezero.data.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninezero.data.model.dto.MyUserDto

@Dao
interface MyUserDao {
    @Query("SELECT * FROM myuserdto LIMIT 1")
    suspend fun getMyUser(): MyUserDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: MyUserDto)

    @Query("DELETE FROM myuserdto")
    suspend fun deleteAll()
}