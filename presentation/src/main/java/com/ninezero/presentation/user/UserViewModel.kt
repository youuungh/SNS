package com.ninezero.presentation.user

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.ChatUseCase
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase,
    private val chatUseCase: ChatUseCase,
    private val networkRepository: NetworkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<UserState, UserSideEffect> {
    private val userId: Long = checkNotNull(savedStateHandle["userId"])

    override val container: Container<UserState, UserSideEffect> = container(initialState = UserState())

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            networkRepository.observeNetworkConnection()
                .collect { isOnline ->
                    if (isOnline && !isInitialLoad) {
                        load()
                    }
                }
        }

        viewModelScope.launch {
            load()
        }
    }

    private fun load() = intent {
        reduce { state.copy(isLoading = true) }
        try {
            when (val result = userUseCase.getUserInfo(userId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            userLoginId = result.data.loginId,
                            username = result.data.userName,
                            profileImageUrl = result.data.profileImagePath,
                            postCount = result.data.postCount,
                            followerCount = result.data.followerCount,
                            followingCount = result.data.followingCount,
                            isFollowing = result.data.isFollowing,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    reduce { state.copy(isLoading = false, isRefreshing = false) }
                    postSideEffect(UserSideEffect.ShowSnackbar(result.message))
                }
            }

            when (val result = feedUseCase.getPostsById(userId)) {
                is ApiResult.Success -> {
                    val posts = result.data.cachedIn(viewModelScope)
                    reduce { state.copy(posts = posts) }
                }
                is ApiResult.Error -> postSideEffect(UserSideEffect.ShowSnackbar(result.message))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isRefreshing = false) }
            Timber.e(e)
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(1000)
        load()
    }

    fun checkAndNavigateToChat(
        otherUserId: Long,
        otherUserLoginId: String,
        otherUserName: String,
        otherUserProfilePath: String?
    ) = intent {
        try {
            when (val myUserResult = userUseCase.getMyUser()) {
                is ApiResult.Success -> {
                    when (val result = chatUseCase.checkExistingRoom(otherUserId)) {
                        is ApiResult.Success -> {
                            postSideEffect(UserSideEffect.NavigateToChat(
                                otherUserId = otherUserId,
                                roomId = result.data,
                                otherUserLoginId = otherUserLoginId,
                                otherUserName = otherUserName,
                                otherUserProfilePath = otherUserProfilePath,
                                myUserId = myUserResult.data.id
                            ))
                        }
                        is ApiResult.Error -> {
                            postSideEffect(UserSideEffect.ShowSnackbar(result.message))
                        }
                    }
                }
                is ApiResult.Error -> {
                    postSideEffect(UserSideEffect.ShowSnackbar(myUserResult.message))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun handleFollowClick() = intent {
        try {
            val result = if (state.isFollowing) {
                userUseCase.unfollowUser(userId)
            } else {
                userUseCase.followUser(userId)
            }

            when (result) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isFollowing = !state.isFollowing,
                            followerCount = if (state.isFollowing) {
                                state.followerCount - 1
                            } else {
                                state.followerCount + 1
                            }
                        )
                    }
                }
                is ApiResult.Error -> {
                    postSideEffect(UserSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

@Immutable
data class UserState(
    val userLoginId: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val posts: Flow<PagingData<Post>> = emptyFlow<PagingData<Post>>(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

sealed interface UserSideEffect {
    data class ShowSnackbar(val message: String) : UserSideEffect
    data class NavigateToChat(
        val otherUserId: Long,
        val roomId: String?,
        val otherUserLoginId: String,
        val otherUserName: String,
        val otherUserProfilePath: String?,
        val myUserId: Long
    ) : UserSideEffect
}