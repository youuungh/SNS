package com.ninezero.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
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

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val feedUseCase: FeedUseCase,
    private val userUseCase: UserUseCase,
    private val networkRepository: NetworkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<PostDetailState, PostDetailSideEffect> {
    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val postId: Long = checkNotNull(savedStateHandle["postId"])

    override val container: Container<PostDetailState, PostDetailSideEffect> =
        container(initialState = PostDetailState())

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
        reduce { state.copy(isLoading = true) }
        try {
            val myUserId = userUseCase.getMyUserId()
            reduce { state.copy(myUserId = myUserId) }

            when (val result = feedUseCase.getPostsById(userId)) {
                is ApiResult.Success -> {
                    val posts = result.data
                        .map { pagingData ->
                            pagingData.map { it.toModel() }
                        }
                        .cachedIn(viewModelScope)
                    reduce {
                        state.copy(
                            posts = posts,
                            isLoading = false,
                            isRefreshing = false,
                            initialPostId = postId
                        )
                    }
                }

                is ApiResult.Error -> {
                    reduce { state.copy(isLoading = false, isRefreshing = false) }
                    postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isRefreshing = false) }
            Timber.e(e)
        }
    }

    private fun loadComments(postId: Long) {
        intent { reduce { state.copy(isLoadingComments = true) } }

        viewModelScope.launch {
            delay(600)
            when (val result = feedUseCase.getComments(postId)) {
                is ApiResult.Success -> {
                    val comments = result.data.map { pagingData ->
                        pagingData.map { comment ->
                            val currentReplyCount = container.stateFlow.value.replyCount[comment.id] ?: comment.replyCount
                            comment.copy(replyCount = currentReplyCount)
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
                        postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    private fun loadRepliesForComment(commentId: Long) {
        val postId = container.stateFlow.value.commentsSheetPost?.postId ?: return

        intent {
            if (!state.loadingReplyIds.contains(commentId)) {
                reduce {
                    state.copy(loadingReplyIds = state.loadingReplyIds + commentId)
                }
            }
        }

        viewModelScope.launch {
            when (val result = feedUseCase.getReplies(postId, commentId)) {
                is ApiResult.Success -> {
                    val replies = result.data

                    intent {
                        reduce {
                            state.copy(
                                replies = state.replies + (commentId to replies),
                                loadingReplyIds = state.loadingReplyIds - commentId,
                                replyCount = state.replyCount + (commentId to replies.size)
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    intent {
                        reduce {
                            state.copy(loadingReplyIds = state.loadingReplyIds - commentId)
                        }
                        postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun setTargetCommentId(commentId: Long) = intent {
        reduce { state.copy(targetCommentId = commentId) }
    }

    fun clearTargetCommentId() = intent {
        reduce { state.copy(targetCommentId = null) }
    }

    fun setReplyToComment(comment: Comment?) = intent {
        val parentId = comment?.parentId
        val expandUpdates = if (comment?.depth == 1 && parentId != null) {
            state.expandedCommentIds + (parentId to true)
        } else {
            state.expandedCommentIds
        }

        val targetId = if (comment?.depth == 1) {
            null
        } else {
            comment?.id
        }

        reduce {
            state.copy(
                replyToComment = comment,
                targetCommentId = targetId,
                expandedCommentIds = expandUpdates
            )
        }
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

        viewModelScope.launch {
            delay(100)
            loadComments(post.postId)
        }
    }

    fun hideCommentsSheet() = intent {
        reduce {
            state.copy(
                commentsSheetPost = null,
                comments = emptyFlow(),
                replyToComment = null,
                targetCommentId = null
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
        reduce { state.copy(dialog = PostDetailDialog.DeletePost(post)) }
    }

    fun showDeleteCommentDialog(postId: Long, comment: Comment) = intent {
        reduce { state.copy(dialog = PostDetailDialog.DeleteComment(postId, comment)) }
    }

    fun hideDialog() = intent {
        reduce { state.copy(dialog = PostDetailDialog.Hidden) }
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
                        optionsSheetPost = null,
                        dialog = PostDetailDialog.Hidden
                    )
                }
                load()
            }

            is ApiResult.Error -> {
                postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun onCommentSend(
        postId: Long,
        text: String,
        mentionedUserIds: List<Long>? = null,
        replyToCommentId: Long? = null
    ) = intent {
        val currentComment = state.replyToComment
        val (actualParentId, actualReplyToCommentId, actualMentionedUserIds) = when {
            currentComment?.depth == 1 -> {
                Triple(
                    currentComment.parentId,
                    currentComment.id,
                    mentionedUserIds ?: listOf(currentComment.userId)
                )
            }
            else -> {
                Triple(currentComment?.id, replyToCommentId, mentionedUserIds)
            }
        }

        when (val result = feedUseCase.addComment(
            postId,
            text,
            actualParentId,
            actualMentionedUserIds,
            actualReplyToCommentId
        )) {
            is ApiResult.Success -> {
                val newCommentId = result.data

                if (actualParentId != null && currentComment != null) {
                    val currentReplyCount = state.replyCount[actualParentId] ?: currentComment.replyCount
                    val newReplyCount = currentReplyCount + 1

                    reduce {
                        state.copy(
                            replyCount = state.replyCount + (actualParentId to newReplyCount),
                            replyToComment = null,
                            expandedCommentIds = state.expandedCommentIds + (actualParentId to true),
                            targetCommentId = newCommentId
                        )
                    }

                    viewModelScope.launch {
                        when (val repliesResult = feedUseCase.getReplies(postId, actualParentId)) {
                            is ApiResult.Success -> {
                                val updatedReplies = repliesResult.data

                                intent {
                                    reduce {
                                        state.copy(
                                            replies = state.replies + (actualParentId to updatedReplies)
                                        )
                                    }
                                }

                                refreshCommentWithUpdatedReplyCounts(postId, actualParentId, updatedReplies.size)
                            }

                            is ApiResult.Error -> handleError(repliesResult)
                        }
                    }
                } else {
                    reduce {
                        state.copy(
                            targetCommentId = newCommentId
                        )
                    }
                    loadComments(postId)
                }
            }

            is ApiResult.Error -> handleError(result)
        }
    }

    private fun refreshCommentWithUpdatedReplyCounts(postId: Long, parentId: Long, replyCount: Int) {
        viewModelScope.launch {
            when (val commentsResult = feedUseCase.getComments(postId)) {
                is ApiResult.Success -> {
                    val updatedComments = commentsResult.data.map { pagingData ->
                        pagingData.map { comment ->
                            if (comment.id == parentId) {
                                comment.copy(replyCount = replyCount)
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
    }

    fun onDeleteComment(postId: Long, comment: Comment) = intent {
        val currentCount = state.commentCount[postId] ?: state.commentsSheetPost?.commentCount ?: 0
        val parentId = comment.parentId

        when (val result = feedUseCase.deleteComment(postId, comment.id)) {
            is ApiResult.Success -> {
                if (parentId != null) {
                    val parentReplyCount = (state.replyCount[parentId] ?: 1) - 1

                    reduce {
                        state.copy(
                            commentCount = state.commentCount + (postId to currentCount - 1),
                            replyCount = state.replyCount + (parentId to parentReplyCount),
                            dialog = PostDetailDialog.Hidden
                        )
                    }

                    if (parentReplyCount > 0) {
                        loadRepliesForComment(parentId)
                    } else {
                        reduce {
                            state.copy(
                                expandedCommentIds = state.expandedCommentIds - parentId,
                                replies = state.replies - parentId
                            )
                        }
                    }

                    refreshCommentWithUpdatedReplyCounts(postId, parentId, parentReplyCount)
                } else {
                    reduce {
                        state.copy(
                            commentCount = state.commentCount + (postId to currentCount - 1),
                            expandedCommentIds = state.expandedCommentIds - comment.id,
                            replies = state.replies - comment.id,
                            replyCount = state.replyCount - comment.id,
                            dialog = PostDetailDialog.Hidden
                        )
                    }

                    loadComments(postId)
                }
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
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

                is ApiResult.Error -> postSideEffect(PostDetailSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    private fun handleError(error: ApiResult.Error) = intent {
        reduce { state.copy(dialog = PostDetailDialog.Error(error.message)) }
        viewModelScope.launch {
            delay(2000)
            reduce { state.copy(dialog = PostDetailDialog.Hidden) }
        }
        postSideEffect(PostDetailSideEffect.ShowSnackbar(error.message))
    }
}

@Immutable
data class PostDetailState(
    val myUserId: Long = -1L,
    val initialPostId: Long = -1L,
    val posts: Flow<PagingData<PostCardModel>> = emptyFlow(),

    // 좋아요, 저장, 팔로우 상태
    val likesCount: Map<Long, Int> = emptyMap(),
    val isLiked: Map<Long, Boolean> = emptyMap(),
    val isFollowing: Map<Long, Boolean> = emptyMap(),
    val isSaved: Map<Long, Boolean> = emptyMap(),

    // 댓글
    val comments: Flow<PagingData<Comment>> = emptyFlow(),
    val commentCount: Map<Long, Int> = emptyMap(),
    val isLoadingComments: Boolean = false,
    val replyToComment: Comment? = null,
    val expandedCommentIds: Map<Long, Boolean> = emptyMap(),
    val loadingReplyIds: Set<Long> = emptySet(),
    val replies: Map<Long, List<Comment>> = emptyMap(),
    val replyCount: Map<Long, Int> = emptyMap(),
    val commentsSheetPost: PostCardModel? = null,
    val targetCommentId: Long? = null,

    // 옵션
    val dialog: PostDetailDialog = PostDetailDialog.Hidden,
    val optionsSheetPost: PostCardModel? = null,
    val editSheetPost: PostCardModel? = null,
    val isEditing: Boolean = false,

    // 상태
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

sealed interface PostDetailDialog {
    data object Hidden : PostDetailDialog
    data class DeleteComment(val postId: Long, val comment: Comment) : PostDetailDialog
    data class DeletePost(val post: PostCardModel) : PostDetailDialog
    data class Error(val message: String) : PostDetailDialog
}

sealed interface PostDetailSideEffect {
    data class ShowSnackbar(val message: String) : PostDetailSideEffect
}