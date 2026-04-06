package com.mayokunadeniyi.instantweather.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NetworkWeatherCondition(
    var temp: Double,
    val pressure: Double,
    val humidity: Double
) : Parcelable
