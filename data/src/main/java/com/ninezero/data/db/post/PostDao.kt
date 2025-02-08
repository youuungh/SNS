package com.ninezero.data.db.post

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninezero.data.model.dto.PostDto

@Dao
interface PostDao {
    @Query("SELECT * FROM postdto ORDER BY id DESC")
    fun getAll(): PagingSource<Int, PostDto>

    @Query("SELECT * FROM postdto WHERE isMyPost = 1 ORDER BY id DESC")
    fun getMyAll(): PagingSource<Int, PostDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostDto>)

    @Query("DELETE FROM postdto WHERE isMyPost = 0")
    suspend fun deleteAll()

    @Query("DELETE FROM postdto WHERE isMyPost = 1")
    suspend fun deleteMyAll()
}