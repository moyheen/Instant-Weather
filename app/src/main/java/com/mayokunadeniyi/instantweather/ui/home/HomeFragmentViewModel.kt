package com.mayokunadeniyi.instantweather.ui.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayokunadeniyi.instantweather.data.model.LocationModel
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.source.repository.UserPreferencesRepository
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.utils.LocationLiveData
import com.mayokunadeniyi.instantweather.utils.Result
import com.mayokunadeniyi.instantweather.utils.convertKelvinToCelsius
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

/**
 * Created by Mayokun Adeniyi on 2020-01-25.
 */

sealed interface HomeScreenUiState {
    data class Success(
        val weather: Weather,
        @StringRes val selectedTemperatureUnit: Int,
        val time: String
    ) : HomeScreenUiState
    object Error : HomeScreenUiState
    object Loading : HomeScreenUiState
}

@HiltViewModel
class HomeFragmentViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val locationLiveData: LocationLiveData
) : ViewModel() {

    var homeScreenUiState: HomeScreenUiState by mutableStateOf(HomeScreenUiState.Loading)
        private set

    fun fetchLocationLiveData() = locationLiveData

    /**
     *This attempts to get the [Weather] from the local data source,
     * if the result is null, it gets from the remote source.
     * @see refreshWeather
     */
    fun getWeather(location: LocationModel) {
        homeScreenUiState = HomeScreenUiState.Loading
        viewModelScope.launch {
            val selectedTemperatureUnit = userPreferencesRepository.getSelectedTemperatureUnit()
            val result = weatherRepository.getWeather(location, false)
            when {
                result is Result.Success -> {
                    if (result.data != null) {
                        homeScreenUiState = HomeScreenUiState.Success(
                            result.data,
                            selectedTemperatureUnit,
                            currentSystemTime()
                        )
                    } else {
                        refreshWeather(location)
                    }
                }

                result is Result.Error -> homeScreenUiState = HomeScreenUiState.Error
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun currentSystemTime(): String {
        val currentTime = System.currentTimeMillis()
        val date = Date(currentTime)
        val dateFormat = SimpleDateFormat("EEEE MMM d, hh:mm aaa")
        return dateFormat.format(date)
    }

    /**
     * This is called when the user swipes down to refresh.
     * This enables the [Weather] for the current [location] to be received.
     */
    fun refreshWeather(location: LocationModel) {
        homeScreenUiState = HomeScreenUiState.Loading
        viewModelScope.launch {
            val selectedTemperatureUnit = userPreferencesRepository.getSelectedTemperatureUnit()
            val result = weatherRepository.getWeather(location, true)
            when {
                result is Result.Success && result.data != null -> {
                    val weather = result.data.apply {
                        this.networkWeatherCondition.temp =
                            convertKelvinToCelsius(this.networkWeatherCondition.temp)
                    }
                    homeScreenUiState = HomeScreenUiState.Success(
                        weather,
                        selectedTemperatureUnit,
                        currentSystemTime()
                    )

                    weatherRepository.deleteWeatherData()
                    weatherRepository.storeWeatherData(weather)
                }

                else -> homeScreenUiState = HomeScreenUiState.Error
            }
        }
    }

    fun onPullToRefresh() {
        locationLiveData.value?.let { refreshWeather(it) }
    }
}
