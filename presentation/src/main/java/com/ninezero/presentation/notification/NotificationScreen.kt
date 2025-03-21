package com.ninezero.presentation.notification

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ninezero.domain.model.Notification
import com.ninezero.presentation.component.LeftAlignedDetailScaffold
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.ninezero.presentation.R
import com.ninezero.presentation.component.AppendError
import com.ninezero.presentation.component.DeleteAllNotificationDialog
import com.ninezero.presentation.component.DeleteNotificationDialog
import com.ninezero.presentation.component.EmptyNotificationScreen
import com.ninezero.presentation.component.LoadingProgress
import com.ninezero.presentation.component.NetworkErrorScreen
import com.ninezero.presentation.component.NotificationItem
import com.ninezero.presentation.component.NotificationSectionHeader
import com.ninezero.presentation.component.PullToRefreshLayout
import com.ninezero.presentation.component.SNSIconButton
import com.ninezero.presentation.component.SNSSurface
import com.ninezero.presentation.component.bottomsheet.DeleteNotificationBottomSheet
import com.ninezero.presentation.component.bounceClick
import com.ninezero.presentation.theme.LocalTheme
import timber.log.Timber
import java.time.LocalDate

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToUser: (Long) -> Unit,
    onNavigateToPost: (Long, Long, Boolean) -> Unit,
    onNavigateToChat: (otherUserId: Long, roomId: String?, otherUserLoginId: String, otherUserName: String, otherUserProfilePath: String?, myUserId: Long) -> Unit
) {
    val state = viewModel.collectAsState().value
    val notifications = state.notifications.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showDropdown by remember { mutableStateOf(false) }

    val groupedNotifications = remember(notifications.itemCount, state.deletedNotificationIds, state.readNotificationIds) {
        val filteredNotifications = (0 until notifications.itemCount)
            .mapNotNull { index -> notifications[index] }
            .filter { !state.deletedNotificationIds.contains(it.id) }
            .map { notification ->
                if (state.readNotificationIds.contains(notification.id)) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }

        val today = LocalDate.now()
        val categoryMap = mutableMapOf<String, MutableList<Notification>>(
            "오늘" to mutableListOf(),
            "최근 7일" to mutableListOf(),
            "최근 30일" to mutableListOf(),
            "이전 활동" to mutableListOf()
        )

        filteredNotifications.forEach { notification ->
            try {
                val notificationDate = LocalDateTime.parse(notification.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toLocalDate()

                val category = when {
                    notificationDate.isEqual(today) -> "오늘"
                    notificationDate.isAfter(today.minusDays(7)) -> "최근 7일"
                    notificationDate.isAfter(today.minusDays(30)) -> "최근 30일"
                    else -> "이전 활동"
                }

                categoryMap[category]?.add(notification)
            } catch (e: Exception) {
                Timber.e(e, "알림 날짜 파싱 실패: ${notification.id}")
                categoryMap["이전 활동"]?.add(notification)
            }
        }

        categoryMap.entries
            .filter { it.value.isNotEmpty() }
            .map { (category, notifications) ->
                category to notifications.sortedByDescending { notification ->
                    try {
                        LocalDateTime.parse(notification.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    } catch (e: Exception) {
                        LocalDateTime.MIN
                    }
                }
            }
            .sortedBy { (category, _) ->
                when(category) {
                    "오늘" -> 0
                    "최근 7일" -> 1
                    "최근 30일" -> 2
                    else -> 3
                }
            }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NotificationSideEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(sideEffect.message)
                }
            }
            is NotificationSideEffect.NavigateToPost -> {
                onNavigateToPost(sideEffect.userId, sideEffect.postId, sideEffect.showComments)
            }
            is NotificationSideEffect.NavigateToUser -> {
                onNavigateToUser(sideEffect.userId)
            }
            is NotificationSideEffect.NavigateToChat -> {
                onNavigateToChat(
                    sideEffect.otherUserId,
                    sideEffect.roomId,
                    sideEffect.otherUserLoginId,
                    sideEffect.otherUserName,
                    sideEffect.otherUserProfilePath,
                    sideEffect.myUserId
                )
            }
        }
    }

    LeftAlignedDetailScaffold(
        title = stringResource(R.string.notification),
        showBackButton = true,
        onBackClick = onNavigateBack,
        actions = {
            if (groupedNotifications.isNotEmpty()) {
                val isDarkTheme = LocalTheme.current

                Box {
                    SNSIconButton(
                        onClick = { showDropdown = true },
                        imageVector = Icons.Rounded.MoreVert,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        offset = DpOffset(x = (-8).dp, y = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isDarkTheme) {
                                        Modifier.border(
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else Modifier
                                )
                                .bounceClick()
                                .clickable {
                                    showDropdown = false
                                    viewModel.showDeleteAllDialog()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.delete_all_notification),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        snackbarHostState = snackbarHostState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                LoadingProgress()
            } else if (state.isError) {
                NetworkErrorScreen(
                    onRetry = { viewModel.load() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PullToRefreshLayout(
                    refreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() }
                ) {
                    if (groupedNotifications.isEmpty()) {
                        EmptyNotificationScreen()
                    } else {
                        SNSSurface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding(),
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupedNotifications.forEach { (timeframe, notificationList) ->
                                    item(key = "header_$timeframe") {
                                        NotificationSectionHeader(timeframe)
                                    }

                                    items(
                                        count = notificationList.size,
                                        key = { index -> notificationList[index].id }
                                    ) { index ->
                                        val notification = notificationList[index]
                                        NotificationItem(
                                            notification = notification,
                                            onClick = { viewModel.handleNotificationClick(notification) },
                                            onDelete = { viewModel.showDeleteBottomSheet(notification.id) }
                                        )
                                    }

                                    if (timeframe != groupedNotifications.last().first) {
                                        item(key = "spacer_$timeframe") {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }

                                item {
                                    when (notifications.loadState.append) {
                                        is LoadState.Loading -> {
                                            LoadingProgress()
                                        }

                                        is LoadState.Error -> {
                                            AppendError(onRetry = { notifications.retry() })
                                        }

                                        else -> {}
                                        /*
                                        is LoadState.NotLoading -> {
                                            if (append.endOfPaginationReached && notifications.itemCount > 0) {
                                                AppendEnd()
                                            }
                                        }*/
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    when (state.dialog) {
        is NotificationDialog.DeleteNotification -> {
            DeleteNotificationDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDeleteDialog() },
                onConfirm = { viewModel.deleteNotification(state.dialog.notificationId) }
            )
        }

        is NotificationDialog.DeleteAllNotifications -> {
            DeleteAllNotificationDialog(
                openDialog = true,
                onDismiss = { viewModel.hideDeleteAllDialog() },
                onConfirm = { viewModel.deleteAllNotification() }
            )
        }

        NotificationDialog.Hidden -> Unit
    }

    state.deleteBottomSheetNotificationId?.let { notificationId ->
        DeleteNotificationBottomSheet(
            showBottomSheet = true,
            onDismiss = { viewModel.hideDeleteBottomSheet() },
            onDelete = {
                viewModel.showDeleteDialog(notificationId)
                viewModel.hideDeleteBottomSheet()
            }
        )
    }
}