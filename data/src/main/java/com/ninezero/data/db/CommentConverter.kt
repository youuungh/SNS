package com.ninezero.data.db

import androidx.room.TypeConverter
import com.ninezero.data.model.CommentDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CommentConverter {
    @TypeConverter
    fun fromCommentListToJson(commentList: List<CommentDto>): String {
        return Json.encodeToString(commentList)
    }

    @TypeConverter
    fun fromJsonToCommentList(json: String): List<CommentDto> {
        return Json.decodeFromString(json)
    }

    /*** 단일
    @TypeConverter
    fun fromCommentToJson(comment: CommentDto): String {
        return Json.encodeToString(comment)
    }

    @TypeConverter
    fun fromJsonToComment(json: String): CommentDto {
        return Json.decodeFromString(json)
    }
    */
}