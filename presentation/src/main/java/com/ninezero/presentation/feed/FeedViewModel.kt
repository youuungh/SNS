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
import com.ninezero.presentation.model.feed.PostModel
import com.ninezero.presentation.model.feed.toModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<FeedState, FeedSideEffect> {
    override val container: Container<FeedState, FeedSideEffect> =
        container(initialState = FeedState())

    init {
        viewModelScope.launch {
            load()
            synchronizeData()
        }
    }

    private fun load() = intent {
        when (val result = userUseCase.getMyUser()) {
            is ApiResult.Success -> {
                reduce { state.copy(myUserId = result.data.id) }
            }

            is ApiResult.Error.Unauthorized -> {
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                postSideEffect(FeedSideEffect.NavigateToLogin)
                return@intent
            }

            is ApiResult.Error.NotFound -> {
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                postSideEffect(FeedSideEffect.NavigateToLogin)
                return@intent
            }

            is ApiResult.Error -> {
                reduce { state.copy(isRefresh = false) }
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }

        when (val result = feedUseCase.getPosts()) {
            is ApiResult.Success -> {
                val posts = result.data
                    .map { pagingData -> pagingData.map { it.toModel() } }
                    .cachedIn(viewModelScope)

                reduce { state.copy(posts = posts, isRefresh = false) }
            }

            is ApiResult.Error -> {
                reduce { state.copy(isRefresh = false) }
                postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefresh = true) }
        delay(600)

        // 기존 캐시된 데이터 초기화
        reduce {
            state.copy(
                posts = emptyFlow(),
                deletedPostIds = emptySet(),
                addedComments = emptyMap(),
                deletedComments = emptyMap()
            )
        }

        synchronizeData()
        load()
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
        val deletedCommentIds =
            currentState.deletedComments[post.postId]?.map { it.id }?.toSet() ?: emptySet()

        return (initialComments + addedComments).distinctBy { it.id }
            .filterNot { deletedCommentIds.contains(it.id) }
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
                reduce { state.copy(currentDialog = FeedDialog.Hidden) }
            }

            is ApiResult.Error -> handleError(result)
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
        delay(1000)
        reduce { state.copy(currentDialog = FeedDialog.Error(error.message)) }
        viewModelScope.launch {
            delay(2000)
            reduce { state.copy(currentDialog = FeedDialog.Hidden) }
        }
        postSideEffect(FeedSideEffect.ShowSnackbar(error.message))
    }

    private fun synchronizeData() = intent {
        viewModelScope.launch {
            val isOnline = networkRepository.observeNetworkConnection().first()
            if (isOnline) {
                feedUseCase.synchronizeData()
            }
        }
    }
}

@Immutable
data class FeedState(
    val myUserId: Long = -1L,
    val posts: Flow<PagingData<PostModel>> = emptyFlow<PagingData<PostModel>>(),
    val deletedPostIds: Set<Long> = emptySet(),
    val addedComments: Map<Long, List<Comment>> = emptyMap(),
    val deletedComments: Map<Long, List<Comment>> = emptyMap(),
    val isRefresh: Boolean = false,
    val currentDialog: FeedDialog = FeedDialog.Hidden,
    val optionsSheetPost: PostModel? = null,
    val commentsSheetPost: PostModel? = null
)

sealed interface FeedDialog {
    data object Hidden : FeedDialog
    data class DeletePost(val post: PostModel) : FeedDialog
    data class DeleteComment(val postId: Long, val comment: Comment) : FeedDialog
    data class Error(val message: String) : FeedDialog
}

sealed interface FeedSideEffect {
    data class ShowSnackbar(val message: String) : FeedSideEffect
    object NavigateToLogin : FeedSideEffect
}