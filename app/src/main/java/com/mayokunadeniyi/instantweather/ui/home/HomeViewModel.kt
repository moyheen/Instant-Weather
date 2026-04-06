package com.mayokunadeniyi.instantweather.ui.home

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayokunadeniyi.instantweather.data.model.LocationModel
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.utils.LocationManager
import com.mayokunadeniyi.instantweather.utils.Result
import com.mayokunadeniyi.instantweather.utils.convertKelvinToCelsius
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class HomeUiState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val dataFetchState: Boolean = true,
    val error: String? = null
)

sealed class HomeUiEvent {
    data class GetWeather(val location: LocationModel) : HomeUiEvent()
    data class RefreshWeather(val location: LocationModel) : HomeUiEvent()
}

/**
 * Created by Mayokun Adeniyi on 2020-01-25.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val time = currentSystemTime()
    
    val locationUpdates: StateFlow<LocationModel?> = locationManager.getLocationUpdates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.GetWeather -> getWeather(event.location)
            is HomeUiEvent.RefreshWeather -> refreshWeather(event.location)
        }
    }

    fun refreshLocation() {
        locationManager.getLastKnownLocation { loc ->
            if (loc != null) {
                getWeather(loc)
            } else {
                _uiState.update { it.copy(isLoading = false, dataFetchState = false) }
            }
        }
    }


    private fun getWeather(location: LocationModel) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getWeather(location, false)) {
                is Result.Success -> {
                    if (result.data != null) {
                        val weather = result.data
                        _uiState.update { it.copy(
                            isLoading = false,
                            dataFetchState = true,
                            weather = weather
                        ) }
                    } else {
                        refreshWeather(location)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        dataFetchState = false
                    ) }
                }

                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
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

    private fun refreshWeather(location: LocationModel) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getWeather(location, true)) {
                is Result.Success -> {
                    if (result.data != null) {
                        val weather = result.data.apply {
                            this.networkWeatherCondition.temp = convertKelvinToCelsius(this.networkWeatherCondition.temp)
                        }
                        _uiState.update { it.copy(
                            isLoading = false,
                            dataFetchState = true,
                            weather = weather
                        ) }

                        repository.deleteWeatherData()
                        repository.storeWeatherData(weather)
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            dataFetchState = false,
                            weather = null
                        ) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        dataFetchState = false
                    ) }
                }
                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }
}
