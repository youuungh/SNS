package com.ninezero.presentation.profile

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ninezero.presentation.component.EditUsernameDialog
import com.ninezero.presentation.component.SNSEditProfileImage
import com.ninezero.presentation.component.SNSSmallButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.theme.SNSTheme
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.ninezero.presentation.R
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.ItemSection
import com.ninezero.presentation.component.PageLoadingProgress
import com.ninezero.presentation.component.LoadingScreen
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.StatisticItem
import com.ninezero.presentation.component.UserCard
import com.ninezero.presentation.component.bounceClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileImageChange: () -> Unit
) {
    val state = viewModel.collectAsState().value
    val suggestedUsers = state.suggestedUsers.collectAsLazyPagingItems()
    val myPosts = state.myPosts.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()

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
            is ProfileSideEffect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(sideEffect.message) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingScreen(onDismissRequest = {})
        } else {
            PullToRefreshLayout(
                refreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                SNSSurface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
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
                                    imageUrl = state.profileImageUrl,
                                    onClick = {
                                        visualMediaPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )

                                Text(
                                    text = state.username.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                SNSSmallButton(
                                    text = stringResource(R.string.edit_username),
                                    onClick = { viewModel.showEditUsernameDialog() }
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 32.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatisticItem(
                                        count = state.postCount,
                                        label = stringResource(R.string.post)
                                    )
                                    StatisticItem(
                                        count = state.followerCount,
                                        label = stringResource(R.string.follower)
                                    )
                                    StatisticItem(
                                        count = state.followingCount,
                                        label = stringResource(R.string.following)
                                    )
                                }
                            }
                        }

                        if (suggestedUsers.itemCount > 0) {
                            item {
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
                                                UserCard(
                                                    userId = user.userId,
                                                    username = user.userName,
                                                    profileImagePath = user.profileImagePath,
                                                    isFollowing = state.isFollowing[user.userId] ?: user.isFollowing,
                                                    onFollowClick = { viewModel.handleFollowClick(user.userId, user) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (myPosts.loadState.refresh !is LoadState.Loading) {
                            stickyHeader {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = stringResource(R.string.my_post),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }

                            item {
                                if (myPosts.itemCount == 0) {
                                    LoadingProgress()
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenHeightDp * 0.8).dp)
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
                                                        AsyncImage(
                                                            model = ImageRequest.Builder(LocalContext.current)
                                                                .data(post.images.first())
                                                                .crossfade(true)
                                                                .build(),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .aspectRatio(1f)
                                                        )

                                                        if (post.images.size > 1) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(4.dp)
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .align(Alignment.TopEnd),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                // Shadow layer
                                                                Icon(
                                                                    painter = painterResource(R.drawable.ic_multiple),
                                                                    contentDescription = null,
                                                                    tint = Color.Black.copy(alpha = 0.4f),
                                                                    modifier = Modifier
                                                                        .scale(1.2f)
                                                                        .blur(6.dp)
                                                                )

                                                                // Main icon
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
                            }

                            if (myPosts.loadState.append is LoadState.Loading) {
                                item {
                                    PageLoadingProgress()
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
                                        onFollowClick = {}
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