package com.ninezero.presentation.feed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import com.ninezero.presentation.model.PostCardModel
import com.ninezero.presentation.model.toModel
import com.ninezero.presentation.profile.ProfileSideEffect
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
import kotlin.collections.plus

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<FeedState, FeedSideEffect> {
    override val container: Container<FeedState, FeedSideEffect> = container(initialState = FeedState())

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
            isInitialLoad = false
        }
    }

    private fun load() = intent {
        try {
            val isOnline = networkRepository.isNetworkAvailable()

            when (val result = feedUseCase.getPosts()) {
                is ApiResult.Success -> {
                    val posts = result.data
                        .map { pagingData -> pagingData.map { it.toModel() } }
                        .cachedIn(viewModelScope)

                    reduce { state.copy(posts = posts, isRefreshing = false) }

                    if (isOnline) {
                        when (val userResult = userUseCase.getMyUser()) {
                            is ApiResult.Success -> {
                                reduce { state.copy(myUserId = userResult.data.id) }
                            }

                            is ApiResult.Error.Unauthorized -> {
                                postSideEffect(FeedSideEffect.ShowSnackbar(userResult.message))
                                postSideEffect(FeedSideEffect.NavigateToLogin)
                            }

                            is ApiResult.Error.NotFound -> {
                                postSideEffect(FeedSideEffect.ShowSnackbar(userResult.message))
                                postSideEffect(FeedSideEffect.NavigateToLogin)
                            }

                            is ApiResult.Error -> {
                                postSideEffect(FeedSideEffect.ShowSnackbar(userResult.message))
                            }
                        }
                    } else {
                        postSideEffect(FeedSideEffect.ShowSnackbar("네트워크 연결 오류"))
                    }
                }

                is ApiResult.Error -> {
                    reduce { state.copy(isRefreshing = false) }
                    postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isRefreshing = false) }
            Timber.e(e)
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(2000)
        load()
    }

    fun showOptionsSheet(post: PostCardModel) = intent {
        reduce { state.copy(optionsSheetPost = post) }
    }

    fun hideOptionsSheet() = intent {
        reduce { state.copy(optionsSheetPost = null) }
    }

    fun showCommentsSheet(post: PostCardModel) = intent {
        reduce { state.copy(commentsSheetPost = post) }
    }

    fun hideCommentsSheet() = intent {
        reduce { state.copy(commentsSheetPost = null) }
    }

    fun showEditSheet(post: PostCardModel) = intent {
        reduce { state.copy(editSheetPost = post) }
    }

    fun hideEditSheet() = intent {
        reduce { state.copy(editSheetPost = null) }
    }

    fun showDeletePostDialog(post: PostCardModel) = intent {
        reduce { state.copy(dialog = FeedDialog.DeletePost(post)) }
    }

    fun showDeleteCommentDialog(postId: Long, comment: Comment) = intent {
        reduce { state.copy(dialog = FeedDialog.DeleteComment(postId, comment)) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(dialog = FeedDialog.Hidden) }
    }

    fun getCombinedComments(post: PostCardModel): List<Comment> {
        val currentState = container.stateFlow.value
        val initialComments = post.comments.toMutableList()
        val addedComments = currentState.addedComments[post.postId].orEmpty()
        val deletedCommentIds =
            currentState.deletedComments[post.postId]?.map { it.id }?.toSet() ?: emptySet()

        return (initialComments + addedComments).distinctBy { it.id }
            .filterNot { deletedCommentIds.contains(it.id) }
    }

    fun onPostEdit(postId: Long, content: String, images: List<String>) = intent {
        reduce { state.copy(isEditing = true) }
        delay(2000)

        when (val result = feedUseCase.updatePost(postId, content, images)) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(
                        editSheetPost = null,
                        isEditing = false
                    )
                }
                load()
            }
            is ApiResult.Error -> {
                reduce { state.copy(isEditing = false) }
                handleError(result)
            }
        }
    }

    fun onPostDelete(model: PostCardModel) = intent {
        when (val result = feedUseCase.deletePost(model.postId)) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(
                        deletedPostIds = state.deletedPostIds + model.postId,
                        optionsSheetPost = null,
                        dialog = FeedDialog.Hidden
                    )
                }
                load()
            }

            is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
        }
    }

    fun onCommentSend(postId: Long, text: String) = intent {
        when (val result = feedUseCase.addComment(postId, text)) {
            is ApiResult.Success -> {
                when (val userResult = userUseCase.getMyUser()) {
                    is ApiResult.Success -> {
                        val user = userResult.data
                        updateComments(
                            postId = postId,
                            comment = Comment(
                                id = result.data,
                                text = text,
                                userName = user.userName,
                                profileImageUrl = user.profileImagePath
                            ),
                            isDelete = false
                        )
                    }

                    is ApiResult.Error -> handleError(userResult)
                }
            }

            is ApiResult.Error -> handleError(result)
        }
    }

    fun onDeleteComment(postId: Long, comment: Comment) = intent {
        when (val result = feedUseCase.deleteComment(postId, comment.id)) {
            is ApiResult.Success -> {
                updateComments(postId, comment, isDelete = true)
                reduce { state.copy(dialog = FeedDialog.Hidden) }
            }

            is ApiResult.Error -> handleError(result)
        }
    }

    fun handleLikeClick(postId: Long, post: PostCardModel) = intent {
        val isLiked = state.isLiked[postId] ?: post.isLiked
        val likesCount = state.likesCount[postId] ?: post.likesCount

        if (isLiked) {
            when (val result = feedUseCase.unlikePost(postId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isLiked = state.isLiked + (postId to false),
                            likesCount = state.likesCount + (postId to likesCount - 1)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        } else {
            when (val result = feedUseCase.likePost(postId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isLiked = state.isLiked + (postId to true),
                            likesCount = state.likesCount + (postId to likesCount + 1)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun handleFollowClick(userId: Long, post: PostCardModel) = intent {
        val isFollowing = state.isFollowing[userId] ?: post.isFollowing

        if (isFollowing) {
            when (val result = userUseCase.unfollowUser(userId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isFollowing = state.isFollowing + (userId to false)
                        )
                    }
                }
                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
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
                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun handleSavedClick(postId: Long, post: PostCardModel) = intent {
        val isSaved = state.isSaved[postId] ?: post.isSaved

        if (isSaved) {
            when (val result = feedUseCase.unsavePost(postId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isSaved = state.isSaved + (postId to false)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        } else {
            when (val result = feedUseCase.savePost(postId)) {
                is ApiResult.Success -> {
                    reduce {
                        state.copy(
                            isSaved = state.isSaved + (postId to true)
                        )
                    }
                }

                is ApiResult.Error -> postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    private fun updateComments(
        postId: Long,
        comment: Comment,
        isDelete: Boolean
    ) = intent {
        val commentsMap = if (isDelete) state.deletedComments else state.addedComments
        val updatedComments = commentsMap[postId].orEmpty() + comment
        reduce {
            if (isDelete) {
                state.copy(deletedComments = commentsMap + (postId to updatedComments))
            } else {
                state.copy(addedComments = commentsMap + (postId to updatedComments))
            }
        }
    }

    private fun handleError(error: ApiResult.Error) = intent {
        reduce { state.copy(dialog = FeedDialog.Error(error.message)) }
        viewModelScope.launch {
            delay(2000)
            reduce { state.copy(dialog = FeedDialog.Hidden) }
        }
        postSideEffect(FeedSideEffect.ShowSnackbar(error.message))
    }
}

@Immutable
data class FeedState(
    val myUserId: Long = -1L,
    val posts: Flow<PagingData<PostCardModel>> = emptyFlow<PagingData<PostCardModel>>(),
    val deletedPostIds: Set<Long> = emptySet(),
    val addedComments: Map<Long, List<Comment>> = emptyMap(),
    val deletedComments: Map<Long, List<Comment>> = emptyMap(),
    val likesCount: Map<Long, Int> = emptyMap(),
    val isLiked: Map<Long, Boolean> = emptyMap(),
    val isFollowing: Map<Long, Boolean> = emptyMap(),
    val isSaved: Map<Long, Boolean> = emptyMap(),
    val isRefreshing: Boolean = false,
    val isEditing: Boolean = false,
    val dialog: FeedDialog = FeedDialog.Hidden,
    val optionsSheetPost: PostCardModel? = null,
    val commentsSheetPost: PostCardModel? = null,
    val editSheetPost: PostCardModel? = null
)

sealed interface FeedDialog {
    data object Hidden : FeedDialog
    data class DeletePost(val post: PostCardModel) : FeedDialog
    data class DeleteComment(val postId: Long, val comment: Comment) : FeedDialog
    data class Error(val message: String) : FeedDialog
}

sealed interface FeedSideEffect {
    data class ShowSnackbar(val message: String) : FeedSideEffect
    object NavigateToLogin : FeedSideEffect
}