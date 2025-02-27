package com.ninezero.presentation.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Post
import com.ninezero.domain.model.RecentSearch
import com.ninezero.domain.model.User
import com.ninezero.domain.repository.NetworkRepository
import com.ninezero.domain.usecase.FeedUseCase
import com.ninezero.domain.usecase.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val feedUseCase: FeedUseCase,
    private val networkRepository: NetworkRepository
) : ViewModel(), ContainerHost<SearchState, SearchSideEffect> {
    override val container: Container<SearchState, SearchSideEffect> = container(initialState = SearchState())

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentSearches()
        loadExplorePosts()
    }

    private fun loadExplorePosts() = intent {
        try {
            reduce { state.copy(isLoading = true) }

            if (networkRepository.isNetworkAvailable()) {
                when (val result = feedUseCase.getPosts()) {
                    is ApiResult.Success -> {
                        reduce {
                            state.copy(
                                posts = result.data.cachedIn(viewModelScope),
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { state.copy(isLoading = false, isRefreshing = false) }
                        postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
                    }
                }
            } else {
                reduce { state.copy(isLoading = false, isRefreshing = false) }
                postSideEffect(SearchSideEffect.ShowSnackbar("네트워크 연결 오류"))
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false, isRefreshing = false) }
            Timber.e(e)
        }
    }

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        delay(2000)
        loadExplorePosts()
    }

    private fun loadRecentSearches() = intent {
        try {
            when (val result = userUseCase.getRecentSearches()) {
                is ApiResult.Success -> {
                    reduce { state.copy(recentSearches = result.data) }
                }
                is ApiResult.Error -> postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setSearchMode(enabled: Boolean) {
        _isSearchMode.value = enabled
        if (!enabled) {
            clearSearch()
        }
    }

    fun onSearchQueryChange(query: String) = intent {
        reduce { state.copy(searchQuery = query) }

        searchJob?.cancel()
        if (query.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                reduce { state.copy(isLoading = true) }
                delay(300)

                when (val result = userUseCase.searchUsers(query)) {
                    is ApiResult.Success -> {
                        reduce {
                            state.copy(
                                searchResults = result.data.cachedIn(viewModelScope),
                                isLoading = false
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
                    }
                }
            }
        } else {
            reduce { state.copy(searchResults = emptyFlow(), isLoading = false) }
        }
    }

    fun clearSearch() = intent {
        searchJob?.cancel()
        reduce {
            state.copy(
                searchQuery = "",
                searchResults = emptyFlow()
            )
        }
    }

    fun saveRecentSearch(userId: Long) = intent {
        when (val result = userUseCase.saveRecentSearch(userId)) {
            is ApiResult.Success -> {
                loadRecentSearches()
            }
            is ApiResult.Error -> {
                postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
            }
        }
    }

    fun deleteRecentSearch(searchId: Long) = intent {
        when (val result = userUseCase.deleteRecentSearch(searchId)) {
            is ApiResult.Success -> {
                reduce {
                    state.copy(
                        recentSearches = state.recentSearches.filter { it.id != searchId }
                    )
                }
            }
            is ApiResult.Error -> postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
        }
    }

    fun clearRecentSearch() = intent {
        when (val result = userUseCase.clearRecentSearches()) {
            is ApiResult.Success -> {
                reduce { state.copy(recentSearches = emptyList()) }
            }
            is ApiResult.Error -> postSideEffect(SearchSideEffect.ShowSnackbar(result.message))
        }
    }
}

@Immutable
data class SearchState(
    val posts: Flow<PagingData<Post>> = emptyFlow(),
    val searchQuery: String = "",
    val searchResults: Flow<PagingData<User>> = emptyFlow(),
    val recentSearches: List<RecentSearch> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

sealed interface SearchSideEffect {
    data class ShowSnackbar(val message: String) : SearchSideEffect
}