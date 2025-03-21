package com.ninezero.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ninezero.domain.model.RecentSearch
import com.ninezero.presentation.component.ClearSection
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.util.Constants.GRID_SPACING
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.R
import com.ninezero.presentation.component.AppendError
import com.ninezero.presentation.component.EmptyFeedScreen
import com.ninezero.presentation.component.EmptySearchScreen
import com.ninezero.presentation.component.RecentSearchCard
import com.ninezero.presentation.component.UserSearchResultCard
import com.ninezero.presentation.component.bounceClick
import kotlinx.coroutines.delay

@Composable
fun ExploreScreen(
    gridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState,
    searchViewModel: SearchViewModel,
    onNavigateToUser: (Long) -> Unit
) {
    val searchState by searchViewModel.collectAsState()
    val isSearchMode by searchViewModel.isSearchMode.collectAsState()
    val posts = searchState.posts.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isSearchMode) {
        keyboardController?.hide()
        focusManager.clearFocus()
        searchViewModel.clearSearch()
        searchViewModel.setSearchMode(false)
    }

    searchViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        AnimatedContent(
            targetState = isSearchMode,
            transitionSpec = {
                if (targetState) {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                } else {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                }
            }
        ) { searchMode ->
            if (searchMode) {
                SearchScreen(
                    state = searchState,
                    onUserClick = { userId ->
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        scope.launch {
                            delay(100)
                            searchViewModel.saveRecentSearch(userId)
                            onNavigateToUser(userId)
                        }
                    },
                    onSearchItemDelete = searchViewModel::deleteRecentSearch,
                    onClearSearchHistory = searchViewModel::clearRecentSearch,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (searchState.isLoading) {
                        LoadingProgress()
                    } else {
                        PullToRefreshLayout(
                            refreshing = searchState.isRefreshing,
                            onRefresh = { searchViewModel.refresh() }
                        ) {
                            SNSSurface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                if (posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading) {
                                    EmptyFeedScreen()
                                } else {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        modifier = Modifier.fillMaxSize(),
                                        columns = GridCells.Fixed(3),
                                        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
                                        verticalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
                                        contentPadding = PaddingValues(GRID_SPACING.dp)
                                    ) {
                                        items(
                                            count = posts.itemCount,
                                            key = posts.itemKey { it.id }
                                        ) { index ->
                                            posts[index]?.let { post ->
                                                if (post.images.isNotEmpty()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .bounceClick()
                                                            .clickable { /* 상세 페이지 */ }
                                                    ) {
                                                        key(post.images.first()) {
                                                            AsyncImage(
                                                                model = ImageRequest
                                                                    .Builder(LocalContext.current)
                                                                    .data(post.images.first())
                                                                    .crossfade(true)
                                                                    .build(),
                                                                contentDescription = null,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .aspectRatio(1f)
                                                            )
                                                        }

                                                        if (post.images.size > 1) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(4.dp)
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .align(Alignment.TopEnd),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(R.drawable.ic_multiple),
                                                                    contentDescription = null,
                                                                    tint = Color.Black.copy(alpha = 0.6f),
                                                                    modifier = Modifier
                                                                        .scale(1.2f)
                                                                        .blur(6.dp)
                                                                )

                                                                Icon(
                                                                    painter = painterResource(R.drawable.ic_multiple),
                                                                    contentDescription = null,
                                                                    tint = Color.White.copy(alpha = 0.8f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        when (posts.loadState.append) {
                                            is LoadState.Loading -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        LoadingProgress()
                                                    }
                                                }
                                            }
                                            is LoadState.Error -> {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        AppendError(onRetry = { posts.retry() })
                                                    }
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(
    state: SearchState,
    onUserClick: (Long) -> Unit,
    onSearchItemDelete: (Long) -> Unit,
    onClearSearchHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults = state.searchResults.collectAsLazyPagingItems()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        if (state.searchQuery.isEmpty()) {
            RecentSearchesSection(
                recentSearches = state.recentSearches,
                onClearAll = onClearSearchHistory,
                onDeleteItem = onSearchItemDelete,
                onUserClick = onUserClick
            )
        } else {
            when {
                state.isLoading -> {
                    LoadingProgress()
                }
                state.isError -> {
                    LoadingProgress()
                }
                searchResults.itemCount > 0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = searchResults.itemCount,
                            key = searchResults.itemKey { it.id }
                        ) { index ->
                            searchResults[index]?.let { user ->
                                UserSearchResultCard(
                                    user = user,
                                    onClick = { onUserClick(user.id) }
                                )
                            }
                        }

                        when (searchResults.loadState.append) {
                            is LoadState.Loading -> {
                                item { LoadingProgress() }
                            }
                            is LoadState.Error -> {
                                item {
                                    AppendError(onRetry = { searchResults.retry() })
                                }
                            }
                            else -> {}
                        }
                    }
                }
                searchResults.loadState.refresh is LoadState.NotLoading -> {
                    EmptySearchScreen()
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<RecentSearch>,
    onClearAll: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    onUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (recentSearches.isNotEmpty()) {
            ClearSection(
                title = stringResource(R.string.recent_search),
                onClearAllClick = onClearAll
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSearches) { search ->
                        RecentSearchCard(
                            search = search,
                            onDelete = { onDeleteItem(search.id) },
                            onClick = { onUserClick(search.searchedUserId) }
                        )
                    }
                }
            }
        }
    }
}