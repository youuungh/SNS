package com.ninezero.data.retrofit

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.UserDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserService {

    @POST("users/login")
    suspend fun login(@Body requestBody: RequestBody): CommonResponse<String>

    @POST("users/sign-up")
    suspend fun signUp(@Body requestBody: RequestBody): CommonResponse<Long>

    @GET("users/my-page")
    suspend fun getMyPage(): CommonResponse<UserDto>

    @PATCH("users/my-page")
    suspend fun patchMyPage(@Body requestBody: RequestBody): CommonResponse<Long>
}