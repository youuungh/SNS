package com.ninezero.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.ninezero.presentation.R

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

@Composable
fun formatRelativeTime(createdAt: String): String {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val createdAtTime = LocalDateTime.parse(createdAt, formatter)
    val now = LocalDateTime.now()

    val minutesDiff = ChronoUnit.MINUTES.between(createdAtTime, now)
    val hoursDiff = ChronoUnit.HOURS.between(createdAtTime, now)
    val daysDiff = ChronoUnit.DAYS.between(createdAtTime, now)
    val weeksDiff = ChronoUnit.WEEKS.between(createdAtTime, now)
    val monthsDiff = ChronoUnit.MONTHS.between(createdAtTime, now)
    val yearsDiff = ChronoUnit.YEARS.between(createdAtTime, now)

    return when {
        minutesDiff < 1 -> stringResource(R.string.just_now)
        minutesDiff < 60 -> stringResource(R.string.minutes_ago, minutesDiff)
        hoursDiff < 24 -> stringResource(R.string.hours_ago, hoursDiff)
        daysDiff < 7 -> stringResource(R.string.days_ago, daysDiff)
        weeksDiff < 4 -> stringResource(R.string.weeks_ago, weeksDiff)
        monthsDiff < 12 -> stringResource(R.string.months_ago, monthsDiff)
        else -> stringResource(R.string.years_ago, yearsDiff)
    }
}