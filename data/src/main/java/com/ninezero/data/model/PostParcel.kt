package com.ninezero.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PostParcel(
    val title: String,
    val content: String,
    val images: List<ImageParcel>
) : Parcelable