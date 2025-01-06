package com.ninezero.data.retrofit

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.PostDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    suspend fun postComment(
        @Path("id") postId: Long,
        @Body requestBody: RequestBody
    ): CommonResponse<Long>

    @DELETE("boards/{boardId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("boardId") postId: Long,
        @Path("commentId") commentId: Long,
    ): CommonResponse<Long>
}