package com.mayokunadeniyi.instantweather.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColorScheme = darkColorScheme(
    onPrimary = Color.White,
    onTertiary = LightGray
)

private val lightColorScheme = lightColorScheme(
    onPrimary = Color.Black,
    onTertiary = DarkGray
)

@Composable
fun InstantWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        content = content,
        colorScheme = colorScheme,
        typography = googleSansTypography
    )
}