package com.ninezero.data.db.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserRemoteKey(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val prevPage: Int?,
    val nextPage: Int?
)