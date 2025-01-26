package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.PostDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

/** retrofit
interface PostService {

    @GET("boards")
    suspend fun getPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): CommonResponse<List<PostDto>>

    @POST("boards")
    suspend fun createPost(@Body requestBody: RequestBody): CommonResponse<Long>

    @DELETE("boards/{id}")
    suspend fun deletePost(@Path("id") id: Long): CommonResponse<Long>

    @POST("boards/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: Long,
        @Body requestBody: RequestBody
    ): CommonResponse<Long>

    @DELETE("boards/{boardId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("boardId") postId: Long,
        @Path("commentId") commentId: Long,
    ): CommonResponse<Long>
}
*/

class PostService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getPosts(page: Int, size: Int): CommonResponse<List<PostDto>> {
        return client.get("boards") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun createPost(requestBody: Any): CommonResponse<Long> {
        return client.post("boards") {
            setBody(requestBody)
        }.body()
    }

    suspend fun deletePost(id: Long): CommonResponse<Long> {
        return client.delete("boards/$id").body()
    }

    suspend fun addComment(postId: Long, requestBody: Any): CommonResponse<Long> {
        return client.post("boards/$postId/comments") {
            setBody(requestBody)
        }.body()
    }

    suspend fun deleteComment(postId: Long, commentId: Long): CommonResponse<Long> {
        return client.delete("boards/$postId/comments/$commentId").body()
    }

    suspend fun likePost(postId: Long): CommonResponse<Long> {
        return client.post("boards/$postId/like-board").body()
    }

    suspend fun unlikePost(postId: Long): CommonResponse<Long> {
        return client.delete("boards/$postId/like-board").body()
    }
}
