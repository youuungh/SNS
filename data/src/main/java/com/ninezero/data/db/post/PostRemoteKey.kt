package com.ninezero.data.db.post

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKey(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val prevPage: Int?,
    val nextPage: Int?
)