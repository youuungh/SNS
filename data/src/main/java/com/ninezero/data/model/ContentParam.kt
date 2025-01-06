package com.ninezero.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class ContentParam(
    val text: String,
    val images: List<String>
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }
}