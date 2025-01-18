package com.ninezero.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun Modifier.onScroll(
    onScroll: (Float, Float) -> Unit
) = this.nestedScroll(
    remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                onScroll(available.x, available.y)
                return Offset.Zero
            }
        }
    }
)

fun <T: Any> LazyPagingItems<T>.isLoading(): Boolean =
    loadState.refresh is LoadState.Loading

fun <T: Any> LazyPagingItems<T>.isError(): Boolean =
    itemCount == 0 && loadState.refresh is LoadState.Error &&
            loadState.refresh !is LoadState.Loading

fun <T: Any> LazyPagingItems<T>.isEmpty(): Boolean =
    itemCount == 0 && loadState.refresh is LoadState.NotLoading &&
            loadState.append.endOfPaginationReached

fun <T: Any> LazyPagingItems<T>.isNotEmpty(): Boolean =
    itemCount > 0