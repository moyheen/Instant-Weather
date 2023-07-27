package com.mayokunadeniyi.instantweather.data.source.repository

interface UserPreferencesRepository {

    suspend fun getSelectedTemperatureUnit(): Int
}
