package com.mayokunadeniyi.instantweather.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayokunadeniyi.instantweather.data.model.WeatherForecast
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.di.scope.DefaultDispatcher
import com.mayokunadeniyi.instantweather.utils.Result
import com.mayokunadeniyi.instantweather.utils.convertKelvinToCelsius
import com.mayokunadeniyi.instantweather.utils.formatDate
import com.shrikanthravi.collapsiblecalendarview.data.Day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Created by Mayokun Adeniyi on 28/02/2020.
 */

data class ForecastUiState(
    val isLoading: Boolean = false,
    val forecast: List<WeatherForecast>? = null,
    val filteredForecast: List<WeatherForecast> = emptyList(),
    val dataFetchState: Boolean = true,
    val selectedDay: Day? = null
)

sealed class ForecastUiEvent {
    data class GetWeatherForecast(val cityId: Int?) : ForecastUiEvent()
    data class RefreshForecastData(val cityId: Int?) : ForecastUiEvent()
    data class UpdateWeatherForecast(val selectedDay: Day, val list: List<WeatherForecast>) : ForecastUiEvent()
}

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: WeatherRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    fun onEvent(event: ForecastUiEvent) {
        when (event) {
            is ForecastUiEvent.GetWeatherForecast -> getWeatherForecast(event.cityId)
            is ForecastUiEvent.RefreshForecastData -> refreshForecastData(event.cityId)
            is ForecastUiEvent.UpdateWeatherForecast -> updateWeatherForecast(event.selectedDay, event.list)
        }
    }

    private fun getWeatherForecast(cityId: Int?) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getForecastWeather(cityId!!, false)) {
                is Result.Success -> {
                    val forecasts = result.data
                    if (!forecasts.isNullOrEmpty()) {
                        _uiState.update { it.copy(isLoading = false, dataFetchState = true, forecast = forecasts) }
                        
                        // Filter for the current day by default if nothing selected
                        val dayToFilter = _uiState.value.selectedDay ?: getTodayDay()
                        updateWeatherForecast(dayToFilter, forecasts)
                    } else {
                        refreshForecastData(cityId)
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, dataFetchState = false) }
                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun refreshForecastData(cityId: Int?) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getForecastWeather(cityId!!, true)) {
                is Result.Success -> {
                    if (result.data != null) {
                        val forecast = result.data.onEach { forecast ->
                            forecast.networkWeatherCondition.temp =
                                convertKelvinToCelsius(forecast.networkWeatherCondition.temp)
                            forecast.date = forecast.date.formatDate()
                        }
                        _uiState.update { it.copy(isLoading = false, dataFetchState = true, forecast = forecast) }
                        repository.deleteForecastData()
                        repository.storeForecastData(forecast)
                        
                        // Filter for the current selected day (or today if none)
                        val dayToFilter = _uiState.value.selectedDay ?: getTodayDay()
                        updateWeatherForecast(dayToFilter, forecast)
                    } else {
                        _uiState.update { it.copy(isLoading = false, dataFetchState = false, forecast = null) }
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, dataFetchState = false) }
                }

                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun updateWeatherForecast(selectedDay: Day, list: List<WeatherForecast>) {
        viewModelScope.launch(defaultDispatcher) {
            val checkerDay = selectedDay.day
            val checkerMonth = selectedDay.month
            val checkerYear = selectedDay.year

            val format = SimpleDateFormat("d MMM y, h:mma", Locale.ENGLISH)
            val calendar = java.util.Calendar.getInstance()

            val filteredList = list.filter { weatherForecast ->
                val date = format.parse(weatherForecast.date)
                if (date != null) {
                    calendar.time = date
                    val forecastDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    val forecastMonth = calendar.get(java.util.Calendar.MONTH)
                    val forecastYear = calendar.get(java.util.Calendar.YEAR)

                    forecastDay == checkerDay && forecastMonth == checkerMonth && forecastYear == checkerYear
                } else {
                    false
                }
            }
            _uiState.update { it.copy(filteredForecast = filteredList, selectedDay = selectedDay) }
        }
    }

    fun getTodayDay(): Day {
        val today = java.util.Calendar.getInstance()
        return Day(
            today.get(java.util.Calendar.YEAR),
            today.get(java.util.Calendar.MONTH),
            today.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
}
