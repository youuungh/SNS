package com.ninezero.presentation.post

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import com.ninezero.domain.model.Image
import com.ninezero.domain.usecase.PostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postUseCase: PostUseCase
) : ViewModel(), ContainerHost<PostState, PostSideEffect> {
    override val container: Container<PostState, PostSideEffect> = container(initialState = PostState())

    init {
        load()
    }

    fun load() = intent {
        try {
            val images = withContext(IO) {
                postUseCase.getImageList()
            }
            reduce {
                state.copy(
                    selectedImages = images.firstOrNull()?.let { listOf(it) } ?: emptyList(),
                    images = images
                )
            }
        } catch (e: Exception) {
            postSideEffect(PostSideEffect.ShowSnackbar("이미지를 불러올 수 없습니다"))
        }
    }

    fun onSingleImageSelect(image: Image) = intent {
        reduce {
            if (state.selectedImages.firstOrNull() == image) {
                state
            } else {
                state.copy(selectedImages = listOf(image))
            }
        }
    }

    fun onMultiImageSelect(image: Image) = intent {
        reduce {
            if (state.selectedImages.contains(image)) {
                state.copy(selectedImages = state.selectedImages - image)
            } else {
                state.copy(selectedImages = state.selectedImages + image)
            }
        }
    }

    fun toggleMultiSelectMode() = intent {
        val isMultiSelectEnabled = !state.isMultiSelectMode

        if (!isMultiSelectEnabled && state.selectedImages.size > 1) {
            reduce {
                state.copy(
                    isMultiSelectMode = isMultiSelectEnabled,
                    selectedImages = state.selectedImages.firstOrNull()?.let { listOf(it) } ?: emptyList()
                )
            }
        } else {
            reduce {
                state.copy(isMultiSelectMode = isMultiSelectEnabled)
            }
        }
    }

    fun onPostClick() = intent {
        reduce { state.copy(isLoading = true) }
        delay(2000)

        withContext(IO) {
            try {
                val htmlContent = state.richTextState.toHtml()

                postUseCase.createPost(
                    title = "제목없음",
                    content = htmlContent,
                    images = state.selectedImages
                )
                reduce { state.copy(isLoading = false) }
                postSideEffect(PostSideEffect.Finish)
            } catch (e: Exception) {
                reduce { state.copy(isLoading = false) }
                postSideEffect(PostSideEffect.ShowSnackbar("게시물 업로드에 실패했습니다"))
            }
        }
    }
}

@Immutable
data class PostState(
    val richTextState: RichTextState = RichTextState(),
    val selectedImages: List<Image> = emptyList(),
    val images: List<Image> = emptyList(),
    val isMultiSelectMode: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface PostSideEffect {
    data class ShowSnackbar(val message: String) : PostSideEffect
    object Finish : PostSideEffect
}