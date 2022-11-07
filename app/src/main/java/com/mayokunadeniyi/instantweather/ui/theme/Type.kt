package com.mayokunadeniyi.instantweather.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mayokunadeniyi.instantweather.R

val googleSansFamily = FontFamily(Font(resId = R.font.googlesans, FontWeight.Normal))

val googleSansTypography = Typography(
    bodyLarge = TextStyle(fontFamily = googleSansFamily, fontSize = 30.sp),
    bodyMedium = TextStyle(
        fontFamily = googleSansFamily,
        fontSize = 20.sp,
        letterSpacing = 0.17.sp
    ),
    bodySmall = TextStyle(fontFamily = googleSansFamily, fontSize = 14.sp, letterSpacing = 0.2.sp)
)