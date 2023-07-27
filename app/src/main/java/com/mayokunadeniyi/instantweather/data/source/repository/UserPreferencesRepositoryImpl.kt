package com.mayokunadeniyi.instantweather.data.source.repository

import android.content.Context
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val context: Context,
    private val sharedPreferencesHelper: SharedPreferenceHelper
) : UserPreferencesRepository {

    override suspend fun getSelectedTemperatureUnit(): Int {
        val fahrenheitUnit = context.getString(R.string.temp_unit_fahrenheit)
        return if (sharedPreferencesHelper.getSelectedTemperatureUnit() == fahrenheitUnit)
            R.string.temp_value_fahrenheit
        else
            R.string.temp_value_celsius
    }
}
