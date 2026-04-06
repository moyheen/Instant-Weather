package com.mayokunadeniyi.instantweather.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            InstantWeatherTheme {
                MainScreen()
            }
        }
    }
}
