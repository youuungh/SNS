package com.ninezero.presentation.profile

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import com.ninezero.presentation.model.UserCardModel
import com.ninezero.presentation.model.toModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<ProfileState, ProfileSideEffect> {
    override val container: Container<ProfileState, ProfileSideEffect> = container(initialState = ProfileState())

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            networkRepository.observeNetworkConnection()
                .collect { isOnline ->
                    if (isOnline && !isInitialLoad) {
                        load()
                        intent { reduce { state.copy(isSavedPostsLoaded = false) } }
                    }
                }
        }

        viewModelScope.launch {
            load()
            isInitialLoad = false
        }
    }

    private fun load() = intent {
        try {
            val isOnline = networkRepository.isNetworkAvailable()

            when (val result = userUseCase.getMyUser()) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            profileImageUrl = result.data.profileImagePath,
                            username = result.data.userName,
                            postCount = result.data.postCount,
                            followerCount = result.data.followerCount,
                            followingCount = result.data.followingCount,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }

                    if (isOnline) {
                        when (val userResult = userUseCase.getAllUsers()) {
                            is ApiResult.Success -> {
                                val suggestedUsers = userResult.data
                                    .map {
                                        it.map { user -> user.toModel() }
                                            .filter { user ->
                                                !(state.isFollowing[user.userId] ?: user.isFollowing)
                                            }
                                    }
                                    .cachedIn(viewModelScope)

                                reduce { state.copy(suggestedUsers = suggestedUsers) }
                            }
                            is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(userResult.message))
                        }
                    } else {
                        postSideEffect(ProfileSideEffect.ShowSnackbar("네트워크 연결 오류"))
                    }
                }
                is ApiResult.Error.Unauthorized -> {
                    postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
                }
                is ApiResult.Error.NotFound -> {
                    postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
                }
                is ApiResult.Error -> {
                    reduce { state.copy(isLoading = false, isRefreshing = false) }
                    postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
                }
            }

            loadMyPosts()
            reduce { state.copy(isMyPostsLoaded = true) }

        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isRefreshing = false) }
            Timber.e(e)
        }
    }

    private fun loadMyPosts() = intent {
        when (val result = feedUseCase.getMyPosts()) {
            is ApiResult.Success -> {
                val myPosts = result.data.cachedIn(viewModelScope)
                reduce { state.copy(myPosts = myPosts) }
            }
            is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
        }
    }

    fun loadSavedPosts() = intent {
        when (val result = feedUseCase.getSavedPosts()) {
            is ApiResult.Success -> {
                val savedPosts = result.data.cachedIn(viewModelScope)
                reduce {
                    state.copy(
                        savedPosts = savedPosts,
                        isSavedPostsLoaded = true
                    )
                }
            }
            is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(2000)
        load()
    }

    fun onImageChange(uri: Uri?, onSuccess: () -> Unit) = intent {
        uri?.toString()?.let {
            when (val result = userUseCase.setProfileImage(it)) {
                is ApiResult.Success -> {
                    onSuccess()
                    load()
                }

                is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onUsernameChange(username: String) = intent {
        when (val result = userUseCase.setMyUser(userName = username, profileImagePath = null)) {
            is ApiResult.Success -> load()
            is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
        }
    }

    fun handleFollowClick(userId: Long, user: UserCardModel) = intent {
        val isFollowing = state.isFollowing[userId] ?: user.isFollowing

        if (isFollowing) {
            when (val result = userUseCase.unfollowUser(userId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isFollowing = state.isFollowing + (userId to false)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        } else {
            when (val result = userUseCase.followUser(userId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isFollowing = state.isFollowing + (userId to true)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(ProfileSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun showEditUsernameDialog() = intent {
        reduce { state.copy(dialog = ProfileDialog.EditUsername(state.username)) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(dialog = ProfileDialog.Hidden) }
    }
}

@Immutable
data class ProfileState(
    val profileImageUrl: String? = null,
    val username: String = "",
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Map<Long, Boolean> = emptyMap(),
    val isSaved: Map<Long, Boolean> = emptyMap(),
    val suggestedUsers: Flow<PagingData<UserCardModel>> = emptyFlow<PagingData<UserCardModel>>(),
    val myPosts: Flow<PagingData<Post>> = emptyFlow<PagingData<Post>>(),
    val savedPosts: Flow<PagingData<Post>> = emptyFlow<PagingData<Post>>(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val dialog: ProfileDialog = ProfileDialog.Hidden,
    val isMyPostsLoaded: Boolean = false,
    val isSavedPostsLoaded: Boolean = false
)

sealed interface ProfileDialog {
    data object Hidden : ProfileDialog
    data class EditUsername(val initialUsername: String) : ProfileDialog
}

sealed interface ProfileSideEffect {
    data class ShowSnackbar(val message: String) : ProfileSideEffect
}