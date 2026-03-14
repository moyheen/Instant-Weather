package com.mayokunadeniyi.instantweather.ui.home

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayokunadeniyi.instantweather.data.model.LocationModel
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.utils.LocationLiveData
import com.mayokunadeniyi.instantweather.utils.Result
import com.mayokunadeniyi.instantweather.utils.convertKelvinToCelsius
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

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
class HomeFragmentViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    @Inject
    lateinit var locationLiveData: LocationLiveData

    init {
        currentSystemTime()
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val time = currentSystemTime()

    fun fetchLocationLiveData() = locationLiveData

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.GetWeather -> getWeather(event.location)
            is HomeUiEvent.RefreshWeather -> refreshWeather(event.location)
        }
    }

    /**
     *This attempts to get the [Weather] from the local data source,
     * if the result is null, it gets from the remote source.
     * @see refreshWeather
     */
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

    /**
     * This is called when the user swipes down to refresh.
     * This enables the [Weather] for the current [location] to be received.
     */
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
