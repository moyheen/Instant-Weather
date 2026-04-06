package com.mayokunadeniyi.instantweather.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mayokunadeniyi.instantweather.R

val PrimaryColor = Color(0xFF1976D2)
val PrimaryLightColor = Color(0xFF63A4FF)
val PrimaryDarkColor = Color(0xFF004BA0)
val PrimaryTextColor = Color(0xFFFFFFFF)
val AccentColor = Color(0xFFE1E2E1)
val LightGray = Color(0xFF969696)
val DarkGray = Color(0xFF2C2C2C)
val SurfaceColor = Color(0xFF121212)
val BottomSheetColor = Color(0xFF1976D2)
val DarkBottomSheetColor = Color(0xFF0F0F0F)

val GoogleSansFamily = FontFamily(
    Font(R.font.googlesans, FontWeight.Normal)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = PrimaryTextColor,
    onPrimaryContainer = PrimaryTextColor,
    secondary = PrimaryLightColor,
    secondaryContainer = PrimaryColor,
    surface = Color.White,
    surfaceVariant = AccentColor,
    onSurfaceVariant = LightGray,
    background = Color.White,
    onBackground = Color.Black,
    error = Color(0xFFB00020),
    tertiary = DarkGray,
    onTertiary = PrimaryTextColor,
    tertiaryContainer = PrimaryLightColor,
    onTertiaryContainer = Color.White,
    surfaceContainer = BottomSheetColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLightColor,
    onPrimary = Color.Black,
    onPrimaryContainer = PrimaryTextColor,
    secondary = PrimaryLightColor,
    secondaryContainer = SurfaceColor,
    surface = DarkGray,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    background = SurfaceColor,
    onBackground = Color.White,
    error = Color(0xFFCF6679),
    tertiary = AccentColor,
    onTertiary = PrimaryLightColor,
    tertiaryContainer = DarkGray,
    onTertiaryContainer = Color.Black,
    surfaceContainer = DarkBottomSheetColor
)

private val AppTypography = Typography().let { default ->
    Typography(
        displayLarge = default.displayLarge.copy(fontFamily = GoogleSansFamily),
        displayMedium = default.displayMedium.copy(fontFamily = GoogleSansFamily),
        displaySmall = default.displaySmall.copy(fontFamily = GoogleSansFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = GoogleSansFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = GoogleSansFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = GoogleSansFamily),
        titleLarge = default.titleLarge.copy(fontFamily = GoogleSansFamily),
        titleMedium = default.titleMedium.copy(fontFamily = GoogleSansFamily),
        titleSmall = default.titleSmall.copy(fontFamily = GoogleSansFamily),
        bodyLarge = default.bodyLarge.copy(fontFamily = GoogleSansFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = GoogleSansFamily),
        bodySmall = default.bodySmall.copy(fontFamily = GoogleSansFamily),
        labelLarge = default.labelLarge.copy(fontFamily = GoogleSansFamily),
        labelMedium = default.labelMedium.copy(fontFamily = GoogleSansFamily),
        labelSmall = default.labelSmall.copy(fontFamily = GoogleSansFamily)
    )
}

@Composable
fun InstantWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor =
                if (darkTheme) Color.Black.toArgb() else PrimaryDarkColor.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
