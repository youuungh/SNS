package com.ninezero.data.db.post

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ninezero.data.db.CommentConverter
import com.ninezero.data.db.post.mypost.MyPostRemoteKey
import com.ninezero.data.db.post.mypost.MyPostRemoteKeyDao
import com.ninezero.data.model.dto.PostDto

@Database(
    entities = [
        PostDto::class,
        PostRemoteKey::class,
        MyPostRemoteKey::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(CommentConverter::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun myPostRemoteKeyDao(): MyPostRemoteKeyDao
}