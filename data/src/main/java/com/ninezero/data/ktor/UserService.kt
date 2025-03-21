package com.ninezero.data.ktor

import com.ninezero.data.model.CommonResponse
import com.ninezero.data.model.dto.RecentSearchDto
import com.ninezero.data.model.param.LoginParam
import com.ninezero.data.model.param.SignUpParam
import com.ninezero.data.model.param.UpdateMyInfoParam
import com.ninezero.data.model.dto.UserDto
import com.ninezero.data.model.param.SocialLoginParam
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

    suspend fun socialLogin(socialLoginParam: SocialLoginParam): CommonResponse<String> {
        return client.post("users/social-login") {
            setBody(socialLoginParam)
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

    suspend fun updateMyPage(param: UpdateMyInfoParam): CommonResponse<Long> {
        return client.patch("users/my-page") {
            setBody(param)
        }.body()
    }

    suspend fun getAllUsers(page: Int, size: Int): CommonResponse<List<UserDto>> {
        return client.get("users") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getUserInfo(userId: Long): CommonResponse<UserDto> {
        return client.get("users/$userId").body()
    }

    suspend fun followUser(userId: Long): CommonResponse<Long> {
        return client.post("users/$userId/follow").body()
    }

    suspend fun unfollowUser(userId: Long): CommonResponse<Long> {
        return client.delete("users/$userId/follow").body()
    }

    suspend fun searchUsers(query: String, page: Int, size: Int): CommonResponse<List<UserDto>> {
        return client.get("users/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getRecentSearches(): CommonResponse<List<RecentSearchDto>> {
        return client.get("users/recent-searches").body()
    }

    suspend fun saveRecentSearch(userId: Long): CommonResponse<Unit> {
        return client.post("users/recent-searches/$userId").body()
    }

    suspend fun deleteRecentSearch(userId: Long): CommonResponse<Unit> {
        return client.delete("users/recent-searches/$userId").body()
    }

    suspend fun clearRecentSearches(): CommonResponse<Unit> {
        return client.delete("users/recent-searches").body()
    }
}