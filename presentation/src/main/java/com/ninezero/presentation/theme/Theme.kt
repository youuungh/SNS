package com.ninezero.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalTheme = compositionLocalOf { false }

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = snsDarkDefault,
    secondary = snsDefaultMedium,
    tertiary = snsDefaultLight,
    background = snsBackgroundDark, //snsDarkSurface
    surface = snsDarkSurface,
    onPrimary = Color.White,
    onSecondary = snsDarkOnSurface,
    onTertiary = snsDarkOnSurface,
    onBackground = snsDarkOnSurface,
    onSurface = snsDarkOnSurface,
    surfaceVariant = snsDarkSurface,
    onSurfaceVariant = snsDarkOnSurface,
    error = errorDark
)

private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = snsDefault,
    secondary = snsDefaultMedium,
    tertiary = snsDefaultLight,
    background = snsBackgroundLight, //Color(0xFFFFFBFE),
    surface = Color.White, //Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = snsLightOnSurface, //Color(0xFF1C1B1F), //Color.White,
    onTertiary = snsLightOnSurface, //Color(0xFF1C1B1F), //Color.White,
    onBackground = snsLightOnSurface, //Color(0xFF1C1B1F),
    onSurface = snsLightOnSurface, //Color(0xFF1C1B1F),
    surfaceVariant = Color.White,
    onSurfaceVariant = snsLightOnSurface,
    error = errorLight
)

@Composable
fun SNSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}