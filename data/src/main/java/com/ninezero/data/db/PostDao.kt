package com.ninezero.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ninezero.data.model.PostDto

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<PostDto>)

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): PagingSource<Int, PostDto>

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int

    @Query("DELETE FROM posts")
    fun deleteAll()
}