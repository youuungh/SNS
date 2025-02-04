package com.ninezero.data.db.post.mypost

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MyPostRemoteKey(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val prevPage: Int?,
    val nextPage: Int?
)