package com.ninezero.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ninezero.data.model.dto.PostDto

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<PostDto>)

    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): PagingSource<Int, PostDto>

    @Query("SELECT id FROM posts")
    suspend fun getAllPostIds(): List<Long>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Long): PostDto?

    @Update
    suspend fun updatePost(post: PostDto)

    @Query("DELETE FROM posts")
    fun deleteAll()

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getCount(): Int
}