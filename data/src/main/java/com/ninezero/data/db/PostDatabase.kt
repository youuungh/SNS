package com.ninezero.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ninezero.data.model.PostDto

@Database(
    entities = [PostDto::class, RemoteKey::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(CommentConverter::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}