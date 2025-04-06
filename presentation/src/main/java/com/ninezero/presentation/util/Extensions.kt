package com.ninezero.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.ninezero.presentation.R
import timber.log.Timber
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// Date Formatters
private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
private val MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM월 dd일")

private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val dateCache = mutableMapOf<String, LocalDate?>()

// Date Time Utils
private fun parseChatDateTime(dateTime: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(dateTime, ISO_FORMATTER)
    } catch (e: Exception) {
        null
    }
}

fun parseMessageDate(dateStr: String): LocalDate? {
    return dateCache.getOrPut(dateStr) {
        try {
            val datePart = dateStr.split("T").firstOrNull() ?: return@getOrPut null
            LocalDate.parse(datePart)
        } catch (e: Exception) {
            Timber.e(e, "날짜 파싱 실패: $dateStr")
            null
        }
    }
}

@Composable
fun formatRelativeTime(createdAt: String): String {
    val createdAtTime = LocalDateTime.parse(createdAt, ISO_FORMATTER)
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

@Composable
fun formatChatDateTime(dateTime: String): String {
    val dt = parseChatDateTime(dateTime) ?: return dateTime
    val now = LocalDateTime.now()
    val daysDiff = ChronoUnit.DAYS.between(dt.toLocalDate(), now.toLocalDate())
    val amPm = stringResource(if (dt.hour < 12) R.string.chat_time_am else R.string.chat_time_pm)

    val hour = if (dt.hour > 12) dt.hour - 12 else if (dt.hour == 0) 12 else dt.hour
    val timeStr = "$hour:${String.format("%02d", dt.minute)}"

    return when {
        // 오늘 h:mm 오전/오후
        daysDiff == 0L -> stringResource(R.string.chat_time_today, timeStr, amPm)

        // 어제 h:mm 오전/오후
        daysDiff == 1L -> stringResource(R.string.chat_time_yesterday, timeStr, amPm)

        // 일주일 이내: 화 h:mm 오전/오후
        daysDiff < 7L -> {
            val dayName = dt.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            stringResource(R.string.chat_time_day, dayName, timeStr, amPm)
        }

        // 이번 달/올해: MMM월 dd일 h:mm 오전/오후
        dt.year == now.year -> {
            stringResource(R.string.chat_time_date, dt.format(MONTH_DAY_FORMATTER), timeStr, amPm)
        }

        // 작년 이전: yyyy년 MMM월 dd일 h:mm 오전/오후
        else -> {
            stringResource(R.string.chat_time_date, dt.format(DATE_FORMATTER), timeStr, amPm)
        }
    }
}

@Composable
fun formatSimpleChatDateTime(dateTime: String): String {
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
}

@Composable
fun formatChatTime(dateTime: String): String {
    val dt = parseChatDateTime(dateTime) ?: return dateTime
    val amPm = stringResource(if (dt.hour < 12) R.string.chat_time_am else R.string.chat_time_pm)
    val hour = if (dt.hour > 12) dt.hour - 12 else if (dt.hour == 0) 12 else dt.hour

    return stringResource(R.string.chat_time, amPm, hour, dt.minute)
}

// Layout Utils
fun calculateGridHeight(
    itemCount: Int,
    screenHeight: Dp,
    columnsCount: Int = (screenHeight / (Constants.CELL_SIZE + Constants.GRID_SPACING).dp)
        .toInt()
        .coerceAtLeast(1)
): Dp {
    val rowCount = (itemCount + columnsCount - 1) / columnsCount
    return (Constants.CELL_SIZE * rowCount + Constants.GRID_SPACING * (rowCount - 1)).dp
}

// Modifier Utils
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