package com.ninezero.presentation.user

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSOutlinedButton
import com.ninezero.presentation.component.SNSProfileImage
import com.ninezero.presentation.component.StatisticItem
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.component.EmptyUserPostScreen
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.SNSMediumText
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.bounceClick
import com.ninezero.presentation.R
import com.ninezero.presentation.component.LeftAlignedDetailScaffold
import com.ninezero.presentation.component.LoadingError
import com.ninezero.presentation.component.SNSOutlinedToggleButton
import com.ninezero.presentation.util.Constants.APP_BAR_HEIGHT
import com.ninezero.presentation.util.Constants.CELL_SIZE
import com.ninezero.presentation.util.Constants.GRID_SPACING
import com.ninezero.presentation.util.Constants.STICKY_HEADER_HEIGHT
import com.ninezero.presentation.util.calculateGridHeight

@Composable
fun UserScreen(
    userId: Long,
    viewModel: UserViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToChat: (
        otherUserId: Long,
        roomId: String?,
        otherUserLoginId: String,
        otherUserName: String,
        otherUserProfilePath: String?,
        myUserId: Long
    ) -> Unit
) {
    val state = viewModel.collectAsState().value
    val posts = state.posts.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val minGridHeight = screenHeight - ((APP_BAR_HEIGHT + STICKY_HEADER_HEIGHT).dp)

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is UserSideEffect.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.message)
            }
            is UserSideEffect.NavigateToChat -> onNavigateToChat(
                sideEffect.otherUserId,
                sideEffect.roomId,
                sideEffect.otherUserLoginId,
                sideEffect.otherUserName,
                sideEffect.otherUserProfilePath,
                sideEffect.myUserId
            )
        }
    }

    LeftAlignedDetailScaffold(
        title = state.userLoginId,
        showBackButton = true,
        onBackClick = onNavigateBack,
        snackbarHostState = snackbarHostState
    ) {
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
                                        userId = userId,
                                        username = state.username,
                                        userLoginId = state.userLoginId,
                                        profileImageUrl = state.profileImageUrl,
                                        postCount = state.postCount,
                                        followerCount = state.followerCount,
                                        followingCount = state.followingCount,
                                        isFollowing = state.isFollowing,
                                        onFollowClick = { viewModel.handleFollowClick() },
                                        viewModel = viewModel
                                    )
                                }

                                stickyHeader {
                                    SNSSurface(elevation = 2.dp) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                        ) {
                                            SNSMediumText(text = stringResource(id = R.string.post))
                                        }
                                    }
                                }

                                item {
                                    when (posts.loadState.refresh) {
                                        is LoadState.Loading -> {
                                            LoadingProgress(modifier = Modifier.padding(top = 80.dp))
                                        }

                                        is LoadState.Error -> {
                                            LoadingError(
                                                onRetry = { posts.refresh() },
                                                minHeight = minGridHeight,
                                                modifier = Modifier.padding(top = 80.dp)
                                            )
                                        }

                                        else -> {
                                            if (posts.itemCount == 0 && posts.loadState.refresh is LoadState.NotLoading) {
                                                EmptyUserPostScreen(minGridHeight = minGridHeight)
                                            } else {
                                                UserPostItems(posts = posts, minGridHeight = minGridHeight)
                                            }
                                        }
                                    }
                                }

                                if (posts.loadState.append is LoadState.Loading) {
                                    if (posts.loadState.refresh !is LoadState.Loading) {
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
    }
}

@Composable
private fun ProfileHeaderSection(
    userId: Long,
    username: String,
    userLoginId: String,
    profileImageUrl: String?,
    postCount: Int,
    followerCount: Int,
    followingCount: Int,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    viewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        key(profileImageUrl) {
            SNSProfileImage(
                modifier = Modifier.size(100.dp),
                imageUrl = profileImageUrl
            )
        }

        key(username) {
            Text(
                text = username.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatisticItem(
                count = postCount,
                label = stringResource(R.string.post)
            )
            StatisticItem(
                count = followerCount,
                label = stringResource(R.string.follower)
            )
            StatisticItem(
                count = followingCount,
                label = stringResource(R.string.following)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SNSOutlinedToggleButton(
                isFollowing = isFollowing,
                onClick = onFollowClick,
                modifier = Modifier.height(36.dp).weight(1f)
            )

            SNSOutlinedButton(
                text = stringResource(id = R.string.send_message),
                onClick = {
                    viewModel.checkAndNavigateToChat(
                        otherUserId = userId,
                        otherUserLoginId = userLoginId,
                        otherUserName = username,
                        otherUserProfilePath = profileImageUrl
                    )
                },
                modifier = Modifier.height(36.dp).weight(1f)
            )
        }
    }
}

@Composable
private fun UserPostItems(
    posts: LazyPagingItems<Post>,
    minGridHeight: Dp
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridHeight = calculateGridHeight(posts.itemCount, screenHeight)

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minGridHeight, max = minGridHeight + gridHeight)
            .navigationBarsPadding(),
        columns = GridCells.Adaptive(CELL_SIZE.dp),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING.dp),
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
    }
}