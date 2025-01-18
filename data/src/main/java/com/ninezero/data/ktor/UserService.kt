package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.model.param.UpdateMyInfoParam
import com.ninezero.data.model.dto.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

/** retrofit
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
*/

class UserService @Inject constructor(
    private val client: HttpClient
) {
    suspend fun login(loginParam: LoginParam): CommonResponse<String> {
        return client.post("users/login") {
            setBody(loginParam)
        }.body()
    }

    suspend fun signUp(signUpParam: SignUpParam): CommonResponse<Long> {
        return client.post("users/sign-up") {
            setBody(signUpParam)
        }.body()
    }

    suspend fun getMyPage(): CommonResponse<UserDto> {
        return client.get("users/my-page").body()
    }

    suspend fun patchMyPage(param: UpdateMyInfoParam): CommonResponse<Long> {
        return client.patch("users/my-page") {
            setBody(param)
        }.body()
    }
}