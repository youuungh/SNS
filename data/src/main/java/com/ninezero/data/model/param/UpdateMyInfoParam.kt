package com.ninezero.data.model.param

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class UpdateMyInfoParam(
    val userName: String,
    val extraUserInfo: String,
    val profileImagePath: String
) {
    fun toRequestBody(): RequestBody {
        return Json.encodeToString(this).toRequestBody()
    }
}
