package com.mayokunadeniyi.instantweather.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val darkColorScheme = darkColorScheme(
    primary = PrimaryDarkColor,
    onPrimary = Color.White,
    secondary = SecondaryColor,
    onTertiary = LightGray
)

private val lightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.Black,
    secondary = SecondaryColor,
    onTertiary = DarkGray
)

@Composable
fun InstantWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        content = content,
        colorScheme = colorScheme,
        typography = Typography
    )
}
