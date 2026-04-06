package com.mayokunadeniyi.instantweather.ui.forecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.NetworkWeatherCondition
import com.mayokunadeniyi.instantweather.data.model.NetworkWeatherDescription
import com.mayokunadeniyi.instantweather.data.model.WeatherForecast
import com.mayokunadeniyi.instantweather.data.model.Wind
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator
import com.mayokunadeniyi.instantweather.utils.convertCelsiusToFahrenheit
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import timber.log.Timber

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { SharedPreferenceHelper.getInstance(context) }
    val cityId = prefs.getCityId() ?: 0

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(cityId) {
        viewModel.onEvent(ForecastUiEvent.GetWeatherForecast(cityId))
    }

    ForecastScreenContent(
        uiState = uiState,
        hasLocationPermission = hasLocationPermission,
        tempUnit = prefs.getSelectedTemperatureUnit() ?: stringResource(R.string.temp_unit_celsius),
        onRefresh = {
            val updatedCityId = prefs.getCityId() ?: 0
            viewModel.onEvent(ForecastUiEvent.RefreshForecastData(updatedCityId))
        },
        onDaySelected = { day ->
            val list = uiState.forecast
            if (list != null) {
                viewModel.onEvent(ForecastUiEvent.UpdateWeatherForecast(day, list))
            }
        },
        onTodayClicked = { viewModel.getTodayDay() }
    )
}

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForecastScreenContent(
    uiState: ForecastUiState,
    hasLocationPermission: Boolean,
    tempUnit: String,
    onRefresh: () -> Unit,
    onDaySelected: (Day) -> Unit,
    onTodayClicked: () -> Day
) {
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                containerColor = MaterialTheme.colorScheme.background,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val calendarPrimaryColor = MaterialTheme.colorScheme.onTertiaryContainer.toArgb()
            val calendarTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
            AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(R.layout.layout_calendar, null) as CollapsibleCalendar
                    view.apply {
                        primaryColor = calendarPrimaryColor
                        textColor = calendarTextColor
                        setCalendarListener(object : CollapsibleCalendar.CalendarListener {
                            override fun onClickListener() {}
                            override fun onDataUpdate() {}
                            override fun onDayChanged() {}
                            override fun onItemClick(v: android.view.View) {}
                            override fun onMonthChange() {}
                            override fun onWeekChange(position: Int) {}
                            override fun onDaySelect() {
                                runCatching {
                                    val selectedDay = this@apply.selectedDay
                                    if (selectedDay != null) {
                                        onDaySelected(selectedDay)
                                    }
                                }.onFailure {
                                    Timber.d(it)
                                }
                            }
                        })

                        // Handle the Today icon explicitly because the library only redirects the month but doesn't select the day
                        val todayIconId =
                            resources.getIdentifier("today_icon", "id", ctx.packageName)
                        if (todayIconId != 0) {
                            findViewById<android.view.View>(todayIconId)?.setOnTouchListener { _, event ->
                                if (event.action == android.view.MotionEvent.ACTION_UP) {
                                    val todayDay = onTodayClicked()
                                    this@apply.select(todayDay)
                                    onDaySelected(todayDay)
                                }
                                false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && uiState.forecast.isNullOrEmpty() -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.loading_text))
                        }
                    }

                    !uiState.dataFetchState && uiState.forecast.isNullOrEmpty() -> {
                        Text(
                            text = if (!hasLocationPermission) {
                                stringResource(R.string.access_location_message)
                            } else {
                                stringResource(R.string.error_occurred)
                            },
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }

                    !uiState.forecast.isNullOrEmpty() -> {
                        val isFahrenheit = tempUnit == stringResource(R.string.temp_unit_fahrenheit)
                        val listToDisplay = uiState.filteredForecast

                        if (listToDisplay.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_forecast_info),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(listToDisplay) { forecast ->
                                    ForecastCard(
                                        forecast = forecast,
                                        isFahrenheit = isFahrenheit
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Text(text = stringResource(R.string.no_forecast_info))
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastCard(forecast: WeatherForecast, isFahrenheit: Boolean) {
    val displayTemp = if (isFahrenheit) {
        convertCelsiusToFahrenheit(forecast.networkWeatherCondition.temp)
    } else {
        forecast.networkWeatherCondition.temp
    }
    val tempSymbol =
        if (isFahrenheit) stringResource(R.string.temp_symbol_fahrenheit) else stringResource(R.string.temp_symbol_celsius)
    val description = forecast.networkWeatherDescription.firstOrNull()
    val descriptionText = description?.description ?: ""
    val mainText = description?.main ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_cloud),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 170.dp, height = 124.dp)
                    .align(Alignment.TopStart)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_big_cloud),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_cloud),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 170.dp, height = 124.dp)
                    .align(Alignment.TopEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = mainText,
                            fontSize = 27.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Text(
                            text = descriptionText,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }

                    val weatherIconColor = MaterialTheme.colorScheme.onTertiary.toArgb()
                    AndroidView(
                        factory = { ctx ->
                            WeatherIconView(ctx).apply {
                                setIconSize(50)
                                setIconColor(weatherIconColor)
                                WeatherIconGenerator.getIconResources(ctx, this, descriptionText)
                            }
                        },
                        update = { view ->
                            WeatherIconGenerator.getIconResources(
                                view.context,
                                view,
                                descriptionText
                            )
                        },
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${displayTemp.toInt()}$tempSymbol",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ForecastDetail(
                            text = "${forecast.networkWeatherCondition.humidity}${stringResource(R.string.humidity_symbol)}",
                            iconRes = R.drawable.ic_humidity
                        )
                        ForecastDetail(
                            text = "${forecast.networkWeatherCondition.pressure}${stringResource(R.string.pressure_symbol)}",
                            iconRes = R.drawable.ic_pressure
                        )
                        ForecastDetail(
                            text = "${forecast.wind.speed}${stringResource(R.string.wind_speed_symbol)}",
                            iconRes = R.drawable.ic_wind
                        )
                    }
                }

                Text(
                    text = forecast.date,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 10.dp, end = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun ForecastDetail(text: String, iconRes: Int) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .size(14.dp)
            .padding(end = 4.dp),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
    )
    Text(
        text = text,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(end = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    val mockForecast = WeatherForecast(
        uID = 1,
        date = "15 Mar 2026, 3:00PM",
        wind = Wind(speed = 5.0, deg = 100),
        networkWeatherDescription = listOf(
            NetworkWeatherDescription(
                id = 1,
                main = "Clouds",
                description = "broken clouds",
                icon = "04d"
            )
        ),
        networkWeatherCondition = NetworkWeatherCondition(
            temp = 25.0,
            pressure = 1010.0,
            humidity = 60.0
        )
    )
    InstantWeatherTheme {
        ForecastScreenContent(
            uiState = ForecastUiState(
                isLoading = false,
                forecast = listOf(mockForecast),
                filteredForecast = listOf(mockForecast),
                dataFetchState = true
            ),
            hasLocationPermission = true,
            tempUnit = "Celsius",
            onRefresh = {},
            onDaySelected = {},
            onTodayClicked = { Day(2026, 2, 15) }
        )
    }
}
