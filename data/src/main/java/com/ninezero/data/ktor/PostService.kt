package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.CommentDto
import com.ninezero.data.model.dto.PostDto
import com.ninezero.data.model.param.CommentParam
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
    suspend fun createPost(requestBody: Any): CommonResponse<Long> {
        return client.post("boards") {
            setBody(requestBody)
        }.body()
    }

    suspend fun getPosts(page: Int, size: Int): CommonResponse<List<PostDto>> {
        return client.get("boards") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getMyPosts(page: Int, size: Int): CommonResponse<List<PostDto>> {
        return client.get("boards/my-boards") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getSavedPosts(page: Int, size: Int): CommonResponse<List<PostDto>> {
        return client.get("boards/saved-boards") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getPostsById(userId: Long, page: Int, size: Int): CommonResponse<List<PostDto>> {
        return client.get("boards/user/$userId") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun updatePost(id: Long, requestBody: Any): CommonResponse<Long> {
        return client.patch("boards/$id") {
            setBody(requestBody)
        }.body()
    }

    suspend fun deletePost(id: Long): CommonResponse<Long> {
        return client.delete("boards/$id").body()
    }

    suspend fun getComments(postId: Long, page: Int, size: Int): CommonResponse<List<CommentDto>> {
        return client.get("boards/$postId/comments") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getReplies(postId: Long, parentId: Long): CommonResponse<List<CommentDto>> {
        return client.get("boards/$postId/comments/$parentId/replies").body()
    }

    suspend fun addComment(postId: Long, requestBody: CommentParam): CommonResponse<Long> {
        return client.post("boards/$postId/comments") {
            setBody(requestBody)
        }.body()
    }

    suspend fun deleteComment(postId: Long, commentId: Long): CommonResponse<Long> {
        return client.delete("boards/$postId/comments/$commentId").body()
    }

    suspend fun likePost(postId: Long): CommonResponse<Long> {
        return client.post("boards/$postId/like").body()
    }

    suspend fun unlikePost(postId: Long): CommonResponse<Long> {
        return client.delete("boards/$postId/like").body()
    }

    suspend fun savePost(postId: Long): CommonResponse<Long> {
        return client.post("boards/$postId/save").body()
    }

    suspend fun unsavePost(postId: Long): CommonResponse<Long> {
        return client.delete("boards/$postId/save").body()
    }
}