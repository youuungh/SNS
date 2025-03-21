package com.ninezero.data.model.param

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class CommentParam(
    val comment: String,
    val parentId: Long? = null,
    val mentionedUserIds: List<Long>? = null,
    val replyToCommentId: Long? = null
) {
    fun toRequestBody(): RequestBody {
        return Json.encodeToString(this).toRequestBody()
    }
}