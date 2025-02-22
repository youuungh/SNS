package com.ninezero.presentation.profile

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ninezero.domain.model.Post
import com.ninezero.presentation.component.EditUsernameDialog
import com.ninezero.presentation.component.SNSEditProfileImage
import com.ninezero.presentation.component.SNSSmallButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.R
import com.ninezero.presentation.component.EmptyMyPostScreen
import com.ninezero.presentation.component.EmptySavedPostScreen
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.ItemSection
import com.ninezero.presentation.component.LoadingError
import com.ninezero.presentation.component.LoadingGridProgress
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.StatisticItem
import com.ninezero.presentation.component.UserCard
import com.ninezero.presentation.component.bounceClick
import com.ninezero.presentation.model.UserCardModel
import com.ninezero.presentation.util.Constants.APP_BAR_HEIGHT
import com.ninezero.presentation.util.Constants.BOTTOM_BAR_HEIGHT
import com.ninezero.presentation.util.Constants.CELL_SIZE
import com.ninezero.presentation.util.Constants.GRID_SPACING
import com.ninezero.presentation.util.Constants.STICKY_TAP_HEADER_HEIGHT
import com.ninezero.presentation.util.calculateGridHeight

@Composable
fun ProfileScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileImageChange: () -> Unit,
    onNavigateToUser: (Long) -> Unit
) {
    val state = viewModel.collectAsState().value
    val suggestedUsers = state.suggestedUsers.collectAsLazyPagingItems()
    val myPosts = state.myPosts.collectAsLazyPagingItems()
    val savedPosts = state.savedPosts.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(ProfileTab.POSTS) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val minGridHeight = screenHeight - ((APP_BAR_HEIGHT + STICKY_TAP_HEADER_HEIGHT + BOTTOM_BAR_HEIGHT).dp)

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ProfileTab.entries.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = ProfileTab.entries[pagerState.currentPage]
    }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab.ordinal)

        if (selectedTab == ProfileTab.SAVED && !state.isSavedPostsLoaded) {
            viewModel.loadSavedPosts()
        }
    }

    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onImageChange(it) {
                    onProfileImageChange()
                }
            }
        }
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ProfileSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingProgress()
        } else {
            PullToRefreshLayout(
                refreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.03f))
            ) {
                SNSSurface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                ProfileHeaderSection(
                                    username = state.username,
                                    profileImageUrl = state.profileImageUrl,
                                    postCount = state.postCount,
                                    followerCount = state.followerCount,
                                    followingCount = state.followingCount,
                                    onEditProfileImage = {
                                        visualMediaPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onEditUsername = { viewModel.showEditUsernameDialog() }
                                )
                            }

                            item {
                                SuggestedUsersSection(
                                    suggestedUsers = suggestedUsers,
                                    isFollowing = state.isFollowing,
                                    onFollowClick = viewModel::handleFollowClick,
                                    onNavigateToUser = onNavigateToUser
                                )
                            }

                            stickyHeader(key = "sticky_header") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                                        ProfileTab.entries.forEach { tab ->
                                            Tab(
                                                text = { Text(stringResource(tab.titleRes)) },
                                                selected = selectedTab == tab,
                                                onClick = { selectedTab = tab }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(
                                            min = minGridHeight,
                                            max = minGridHeight + when (selectedTab) {
                                                ProfileTab.POSTS -> calculateGridHeight(myPosts.itemCount, screenHeight)
                                                ProfileTab.SAVED -> calculateGridHeight(savedPosts.itemCount, screenHeight)
                                            }
                                        )
                                ) {
                                    HorizontalPager(state = pagerState) { page ->
                                        when (ProfileTab.entries[page]) {
                                            ProfileTab.POSTS -> {
                                                when (myPosts.loadState.refresh) {
                                                    is LoadState.Loading -> {
                                                        LoadingGridProgress(
                                                            minGridHeight = minGridHeight,
                                                            modifier = Modifier.padding(top = 80.dp)
                                                        )
                                                    }

                                                    else -> {
                                                        if (myPosts.itemCount == 0 && myPosts.loadState.refresh is LoadState.NotLoading) {
                                                            EmptyMyPostScreen(minGridHeight = minGridHeight)
                                                        } else {
                                                            MyPostItems(myPosts = myPosts, minGridHeight = minGridHeight)
                                                        }
                                                    }
                                                }
                                            }
                                            ProfileTab.SAVED -> {
                                                when (savedPosts.loadState.refresh) {
                                                    is LoadState.Loading -> {
                                                        LoadingGridProgress(
                                                            minGridHeight = minGridHeight,
                                                            modifier = Modifier.padding(top = 80.dp)
                                                        )
                                                    }

                                                    is LoadState.Error -> {
                                                        LoadingError(
                                                            onRetry = { savedPosts.refresh() },
                                                            minHeight = minGridHeight,
                                                            modifier = Modifier.padding(top = 80.dp)
                                                        )
                                                    }

                                                    else -> {
                                                        if (savedPosts.itemCount == 0 && savedPosts.loadState.refresh is LoadState.NotLoading) {
                                                            EmptySavedPostScreen(minGridHeight = minGridHeight)
                                                        } else {
                                                            SavedPostItems(savedPosts = savedPosts, minGridHeight = minGridHeight)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (myPosts.loadState.append is LoadState.Loading) {
                                if (myPosts.loadState.refresh !is LoadState.Loading) {
                                    item {
                                        LoadingProgress()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (val dialog = state.dialog) {
        is ProfileDialog.EditUsername -> {
            var username by remember(dialog) { mutableStateOf(dialog.initialUsername) }
            EditUsernameDialog(
                openDialog = true,
                currentUsername = username,
                onUsernameChange = { username = it },
                onDismiss = viewModel::hideDialog,
                onConfirm = {
                    viewModel.onUsernameChange(username)
                    viewModel.hideDialog()
                }
            )
        }

        ProfileDialog.Hidden -> Unit
    }
}

@Composable
private fun ProfileHeaderSection(
    username: String,
    profileImageUrl: String?,
    postCount: Int,
    followerCount: Int,
    followingCount: Int,
    onEditProfileImage: () -> Unit,
    onEditUsername: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        key(profileImageUrl) {
            SNSEditProfileImage(
                modifier = Modifier.size(100.dp),
                imageUrl = profileImageUrl,
                onClick = onEditProfileImage
            )
        }

        key(username) {
            Text(
                text = username.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        SNSSmallButton(
            text = stringResource(R.string.edit_username),
            onClick = onEditUsername
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            key(postCount) {
                StatisticItem(
                    count = postCount,
                    label = stringResource(R.string.post)
                )
            }
            key(followerCount) {
                StatisticItem(
                    count = followerCount,
                    label = stringResource(R.string.follower)
                )
            }
            key(followingCount) {
                StatisticItem(
                    count = followingCount,
                    label = stringResource(R.string.following)
                )
            }
        }
    }
}

@Composable
private fun SuggestedUsersSection(
    suggestedUsers: LazyPagingItems<UserCardModel>,
    isFollowing: Map<Long, Boolean>,
    onFollowClick: (Long, UserCardModel) -> Unit,
    onNavigateToUser: (Long) -> Unit
) {
    if (suggestedUsers.itemCount > 0) {
        ItemSection(
            title = stringResource(R.string.suggested_users),
            onShowAllClick = { /* 모두 보기 */ }
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = minOf(5, suggestedUsers.itemCount),
                    key = suggestedUsers.itemKey { it.userId }
                ) { index ->
                    suggestedUsers[index]?.let { user ->
                        key(user.userId) {
                            UserCard(
                                userId = user.userId,
                                username = user.userName,
                                profileImagePath = user.profileImagePath,
                                isFollowing = isFollowing[user.userId] ?: user.isFollowing,
                                onFollowClick = { onFollowClick(user.userId, user) },
                                onUserClick = { onNavigateToUser(user.userId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyPostItems(
    myPosts: LazyPagingItems<Post>,
    minGridHeight: Dp
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridHeight = calculateGridHeight(myPosts.itemCount, screenHeight)

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minGridHeight, max = minGridHeight + gridHeight),
        columns = GridCells.Adaptive(CELL_SIZE.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING.dp)
    ) {
        items(
            count = myPosts.itemCount,
            key = myPosts.itemKey { it.id }
        ) { index ->
            myPosts[index]?.let { post ->
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
    }
}

@Composable
private fun SavedPostItems(
    savedPosts: LazyPagingItems<Post>,
    minGridHeight: Dp
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridHeight = calculateGridHeight(savedPosts.itemCount, screenHeight)

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minGridHeight, max = minGridHeight + gridHeight),
        columns = GridCells.Adaptive(CELL_SIZE.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING.dp)
    ) {
        items(
            count = savedPosts.itemCount,
            key = savedPosts.itemKey { it.id }
        ) { index ->
            savedPosts[index]?.let { post ->
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
    }
}

enum class ProfileTab(@StringRes val titleRes: Int) {
    POSTS(R.string.my_post),
    SAVED(R.string.my_saved)
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview() {
    SNSTheme {
        SNSSurface {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SNSEditProfileImage(
                                modifier = Modifier.size(100.dp),
                                imageUrl = null,
                                onClick = {}
                            )

                            Text(
                                text = "Preview User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            SNSSmallButton(
                                text = stringResource(R.string.edit_username),
                                onClick = {}
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatisticItem(
                                    count = 100,
                                    label = stringResource(R.string.post)
                                )
                                StatisticItem(
                                    count = 100,
                                    label = stringResource(R.string.follower)
                                )
                                StatisticItem(
                                    count = 100,
                                    label = stringResource(R.string.following)
                                )
                            }
                        }
                    }

                    item {
                        ItemSection(
                            title = stringResource(R.string.suggested_users),
                            onShowAllClick = {}
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(5) { index ->
                                    UserCard(
                                        userId = index.toLong(),
                                        username = "User $index",
                                        profileImagePath = null,
                                        isFollowing = index % 2 == 0,
                                        onFollowClick = {},
                                        onUserClick = {}
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "내 게시물",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}