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

    private fun loadComments(postId: Long) {
        viewModelScope.launch {
            delay(600)
            when (val result = feedUseCase.getComments(postId)) {
                is ApiResult.Success -> {
                    val currentReplyCount = container.stateFlow.value.replyCount
                    val comments = result.data.map { pagingData ->
                        pagingData.map { comment ->
                            comment.copy(
                                replyCount = currentReplyCount[comment.id] ?: comment.replyCount
                            )
                        }
                    }.cachedIn(viewModelScope)

                    intent {
                        reduce {
                            state.copy(
                                comments = comments,
                                isLoadingComments = false
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    intent {
                        reduce { state.copy(isLoadingComments = false) }
                        postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    private fun loadRepliesForComment(commentId: Long) {
        val postId = container.stateFlow.value.commentsSheetPost?.postId ?: return

        intent {
            reduce {
                state.copy(loadingReplyIds = state.loadingReplyIds + commentId)
            }
        }

        viewModelScope.launch {
            when (val result = feedUseCase.getReplies(postId, commentId)) {
                is ApiResult.Success -> {
                    intent {
                        reduce {
                            state.copy(
                                replies = state.replies + (commentId to result.data),
                                loadingReplyIds = state.loadingReplyIds - commentId,
                                replyCount = state.replyCount + (commentId to result.data.size)
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    intent {
                        reduce {
                            state.copy(loadingReplyIds = state.loadingReplyIds - commentId)
                        }
                        postSideEffect(FeedSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun setReplyToComment(comment: Comment?) = intent {
        reduce { state.copy(replyToComment = comment) }
    }

    fun toggleRepliesVisibility(commentId: Long) = intent {
        val currentVisibility = state.expandedCommentIds[commentId] == true

        reduce {
            state.copy(
                expandedCommentIds = if (currentVisibility) {
                    state.expandedCommentIds - commentId
                } else {
                    state.expandedCommentIds + (commentId to true)
                }
            )
        }

        if (!currentVisibility) {
            loadRepliesForComment(commentId)
        }
    }

    fun showOptionsSheet(post: PostCardModel) = intent {
        reduce { state.copy(optionsSheetPost = post) }
    }

    fun hideOptionsSheet() = intent {
        reduce { state.copy(optionsSheetPost = null) }
    }

    fun showCommentsSheet(post: PostCardModel) = intent {
        reduce {
            state.copy(
                commentsSheetPost = post,
                isLoadingComments = true
            )
        }
        loadComments(post.postId)
    }

    fun hideCommentsSheet() = intent {
        reduce {
            state.copy(
                commentsSheetPost = null,
                comments = emptyFlow(),
                commentCount = emptyMap(),
                replyToComment = null,
                expandedCommentIds = emptyMap(),
                loadingReplyIds = emptySet(),
                replies = emptyMap(),
                replyCount = emptyMap()
            )
        }
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
        val parentId = state.replyToComment?.id
        val currentComment = state.replyToComment

        when (val result = feedUseCase.addComment(postId, text, parentId)) {
            is ApiResult.Success -> {
                if (parentId != null && currentComment != null) {
                    val currentReplyCount = state.replyCount[parentId] ?: currentComment.replyCount
                    val newReplyCount = currentReplyCount + 1

                    reduce {
                        state.copy(
                            replyCount = state.replyCount + (parentId to newReplyCount),
                            replyToComment = null,
                            expandedCommentIds = state.expandedCommentIds + (parentId to true)
                        )
                    }

                    viewModelScope.launch {
                        when (val repliesResult = feedUseCase.getReplies(postId, parentId)) {
                            is ApiResult.Success -> {
                                intent {
                                    reduce {
                                        state.copy(
                                            replies = state.replies + (parentId to repliesResult.data)
                                        )
                                    }
                                }

                                when (val commentsResult = feedUseCase.getComments(postId)) {
                                    is ApiResult.Success -> {
                                        val updatedComments = commentsResult.data.map { pagingData ->
                                            pagingData.map { comment ->
                                                if (comment.id == parentId) {
                                                    comment.copy(replyCount = repliesResult.data.size)
                                                } else {
                                                    comment
                                                }
                                            }
                                        }.cachedIn(viewModelScope)

                                        intent {
                                            reduce {
                                                state.copy(
                                                    comments = updatedComments,
                                                    isLoadingComments = false
                                                )
                                            }
                                        }
                                    }
                                    is ApiResult.Error -> handleError(commentsResult)
                                }
                            }
                            is ApiResult.Error -> handleError(repliesResult)
                        }
                    }
                } else {
                    loadComments(postId)
                }
            }
            is ApiResult.Error -> handleError(result)
        }
    }

    fun onDeleteComment(postId: Long, comment: Comment) = intent {
        val currentCount = state.commentCount[postId] ?: state.commentsSheetPost?.commentCount ?: 0
        val parentId = comment.parentId

        when (val result = feedUseCase.deleteComment(postId, comment.id)) {
            is ApiResult.Success -> {
                if (parentId != null) {
                    val parentReplyCount = (state.replyCount[parentId] ?: 1) - 1

                    when (val commentsResult = feedUseCase.getComments(postId)) {
                        is ApiResult.Success -> {
                            val updatedComments = commentsResult.data.map { pagingData ->
                                pagingData.map { comment ->
                                    if (comment.id == parentId) {
                                        comment.copy(replyCount = parentReplyCount)
                                    } else {
                                        comment
                                    }
                                }
                            }.cachedIn(viewModelScope)

                            reduce {
                                state.copy(
                                    commentCount = state.commentCount + (postId to currentCount - 1),
                                    replyCount = state.replyCount + (parentId to parentReplyCount),
                                    comments = updatedComments,
                                    expandedCommentIds = state.expandedCommentIds - parentId,
                                    replies = state.replies - parentId,
                                    dialog = FeedDialog.Hidden
                                )
                            }

                            if (parentReplyCount > 0) {
                                loadRepliesForComment(parentId)
                            }
                        }
                        is ApiResult.Error -> handleError(commentsResult)
                    }
                } else {
                    reduce {
                        state.copy(
                            commentCount = state.commentCount + (postId to currentCount - 1),
                            expandedCommentIds = state.expandedCommentIds - comment.id,
                            replies = state.replies - comment.id,
                            replyCount = state.replyCount - comment.id,
                            dialog = FeedDialog.Hidden
                        )
                    }
                    loadComments(postId)
                }
            }
            is ApiResult.Error -> {
                handleError(result)
            }
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
    val posts: Flow<PagingData<PostCardModel>> = emptyFlow(),
    val deletedPostIds: Set<Long> = emptySet(),

    // comment
    val comments: Flow<PagingData<Comment>> = emptyFlow(),
    val commentCount: Map<Long, Int> = emptyMap(),
    val isLoadingComments: Boolean = false,
    val replyToComment: Comment? = null,
    val expandedCommentIds: Map<Long, Boolean> = emptyMap(),
    val loadingReplyIds: Set<Long> = emptySet(),
    val replies: Map<Long, List<Comment>> = emptyMap(),
    val replyCount: Map<Long, Int> = emptyMap(),

    // like & save
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