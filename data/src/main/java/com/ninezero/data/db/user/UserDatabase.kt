package com.ninezero.data.db.user

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ninezero.data.model.dto.MyUserDto
import com.ninezero.data.model.dto.UserDto

@Database(
    entities = [
        UserDto::class,
        UserRemoteKey::class,
        MyUserDto::class
    ],
    version = 1,
    exportSchema = false
)

abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userRemoteKeyDao(): UserRemoteKeyDao
    abstract fun myUserDao(): MyUserDao
}