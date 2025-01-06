package com.ninezero.data.model

import android.os.Parcelable
import com.ninezero.domain.model.Image
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ImageParcel(
    val uri: String,
    val name: String,
    val size: Long,
    val mimeType: String
) : Parcelable {
    fun toImage() = Image(uri, name, size, mimeType)

    companion object {
        fun fromImage(image: Image) = ImageParcel(
            uri = image.uri,
            name = image.name,
            size = image.size,
            mimeType = image.mimeType
        )
    }
}