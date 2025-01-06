package com.ninezero.presentation.feed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Comment
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import com.ninezero.presentation.model.feed.PostModel
import com.ninezero.presentation.model.feed.toModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase
) : ViewModel(), ContainerHost<FeedState, FeedSideEffect> {
    override val container: Container<FeedState, FeedSideEffect> = container(initialState = FeedState())

    private val _showNewPost = MutableStateFlow(false)
    val showNewPost: StateFlow<Boolean> = _showNewPost

    init {
        load(true)
    }

    private fun load(isLoading: Boolean = false) = intent {
        if (isLoading) {
            reduce { state.copy(isLoading = true) }
        }
        reduce { state.copy(hasError = false) }

        when (val result = userUseCase.getMyUser()) {
            is ApiResult.Success -> {
                reduce { state.copy(myUserId = result.data.id) }
            }
            is ApiResult.Error -> {
                reduce {
                    state.copy(
                        hasError = true,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                return@intent
            }
        }

        when (val result = feedUseCase.getPosts()) {
            is ApiResult.Success -> {
                val posts = result.data
                    .map { pagingData -> pagingData.map { it.toModel() } }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 2000),
                        initialValue = PagingData.empty()
                    )
                    .cachedIn(viewModelScope)

                reduce {
                    state.copy(
                        posts = posts,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
            is ApiResult.Error -> {
                reduce {
                    state.copy(
                        hasError = true,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun reload() = intent {
        _showNewPost.value = true
        load(true)
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(300)
        load(isLoading = false)
    }

    fun showOptionsSheet(post: PostModel) = intent {
        reduce { state.copy(optionsSheetPost = post) }
    }

    fun hideOptionsSheet() = intent {
        reduce { state.copy(optionsSheetPost = null) }
    }

    fun showCommentsSheet(post: PostModel) = intent {
        reduce { state.copy(commentsSheetPost = post) }
    }

    fun hideCommentsSheet() = intent {
        reduce { state.copy(commentsSheetPost = null) }
    }

    fun showDeletePostDialog(post: PostModel) = intent {
        reduce { state.copy(currentDialog = FeedDialog.DeletePost(post)) }
    }

    fun showDeleteCommentDialog(postId: Long, comment: Comment) = intent {
        reduce { state.copy(currentDialog = FeedDialog.DeleteComment(postId, comment)) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(currentDialog = FeedDialog.Hidden) }
    }

    fun getCombinedComments(post: PostModel): List<Comment> {
        val currentState = container.stateFlow.value
        val initialComments = post.comments.toMutableList()
        val addedComments = currentState.addedComments[post.postId].orEmpty()
        val deletedCommentIds = currentState.deletedComments[post.postId]?.map { it.id }?.toSet() ?: emptySet()

        return (initialComments + addedComments).distinctBy { it.id }
            .filterNot { deletedCommentIds.contains(it.id) }
    }

    fun onNewPostBannerClick() = intent {
        _showNewPost.value = false
    }

    fun onPostDelete(model: PostModel) = intent {
        when (val result = feedUseCase.deletePost(model.postId)) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(
                        deletedPostIds = state.deletedPostIds + model.postId,
                        optionsSheetPost = null,
                        currentDialog = FeedDialog.Hidden
                    )
                }
                load(true)
            }
            is ApiResult.Error -> {
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onCommentSend(postId: Long, text: String) = intent {
        when (val result = feedUseCase.postComment(postId, text)) {
            is ApiResult.Success -> {
                when (val userResult = userUseCase.getMyUser()) {
                    is ApiResult.Success -> {
                        val user = userResult.data
                        updateComments(
                            postId = postId,
                            comment = Comment(
                                id = result.data,
                                text = text,
                                username = user.username,
                                profileImageUrl = user.profileImageUrl
                            ),
                            isDelete = false
                        )
                    }
                    is ApiResult.Error -> {
                        postSideEffect(FeedSideEffect.ShowSnackbar(userResult.message))
                    }
                }
            }
            is ApiResult.Error -> {
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onDeleteComment(postId: Long, comment: Comment) = intent {
        when (val result = feedUseCase.deleteComment(postId, comment.id)) {
            is ApiResult.Success -> {
                updateComments(postId, comment, isDelete = true)
                reduce { state.copy(currentDialog = FeedDialog.Hidden) }
            }
            is ApiResult.Error -> {
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
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

    fun updateLoadState(loadState: LoadState) = intent {
        val newIsRefreshing = state.isRefreshing && loadState is LoadState.Loading
        if (state.isRefreshing != newIsRefreshing) {
            reduce {
                state.copy(isRefreshing = newIsRefreshing)
            }
        }
    }
}

@Immutable
data class FeedState(
    val myUserId: Long = -1L,
    val posts: Flow<PagingData<PostModel>> = MutableStateFlow(PagingData.empty()),
    val deletedPostIds: Set<Long> = emptySet(),
    val addedComments: Map<Long, List<Comment>> = emptyMap(),
    val deletedComments: Map<Long, List<Comment>> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasError: Boolean = false,
    val currentDialog: FeedDialog = FeedDialog.Hidden,
    val optionsSheetPost: PostModel? = null,
    val commentsSheetPost: PostModel? = null
)

sealed interface FeedDialog {
    data object Hidden : FeedDialog
    data class DeletePost(val post: PostModel) : FeedDialog
    data class DeleteComment(val postId: Long, val comment: Comment) : FeedDialog
}

sealed interface FeedSideEffect {
    data class ShowSnackbar(val message: String) : FeedSideEffect
}