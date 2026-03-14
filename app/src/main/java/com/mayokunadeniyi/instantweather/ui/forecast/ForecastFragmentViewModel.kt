package com.mayokunadeniyi.instantweather.ui.forecast

import androidx.lifecycle.MutableLiveData
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

/**
 * Created by Mayokun Adeniyi on 28/02/2020.
 */

data class ForecastUiState(
    val isLoading: Boolean = false,
    val forecast: List<WeatherForecast>? = null,
    val filteredForecast: List<WeatherForecast> = emptyList(),
    val dataFetchState: Boolean = true
)

sealed class ForecastUiEvent {
    data class GetWeatherForecast(val cityId: Int?) : ForecastUiEvent()
    data class RefreshForecastData(val cityId: Int?) : ForecastUiEvent()
    data class UpdateWeatherForecast(val selectedDay: Day, val list: List<WeatherForecast>) : ForecastUiEvent()
}

class ForecastFragmentViewModel @Inject constructor(
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
                    if (!result.data.isNullOrEmpty()) {
                        val forecasts = result.data
                        _uiState.update { it.copy(isLoading = false, dataFetchState = true, forecast = forecasts) }
                    } else {
                        refreshForecastData(cityId)
                    }
                }
                else -> _uiState.update { it.copy(isLoading = true) }
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
            selectedDay.let {
                val checkerDay = it.day
                val checkerMonth = it.month
                val checkerYear = it.year

                val filteredList = list.filter { weatherForecast ->
                    val format = SimpleDateFormat("d MMM y, h:mma", Locale.ENGLISH)
                    val formattedDate = format.parse(weatherForecast.date)
                    val weatherForecastDay = formattedDate?.date
                    val weatherForecastMonth = formattedDate?.month
                    val weatherForecastYear = formattedDate?.year
                    // This checks if the selected day, month and year are equal. The year requires an addition of 1900 to get the correct year.
                    weatherForecastDay == checkerDay && weatherForecastMonth == checkerMonth && weatherForecastYear?.plus(
                        1900
                    ) == checkerYear
                }
                _uiState.update { it.copy(filteredForecast = filteredList) }
            }
        }
    }
}
