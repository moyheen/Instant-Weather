package com.mayokunadeniyi.instantweather.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherDetailsScreen(homeFragmentViewModel: HomeFragmentViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fahrenheitUnit = stringResource(id = R.string.temp_unit_fahrenheit)
    val tempValueResId = if (SharedPreferenceHelper.getInstance(context)
            .getSelectedTemperatureUnit() == fahrenheitUnit
    )
        R.string.temp_value_fahrenheit
    else
        R.string.temp_value_celsius
    val weatherState: State<Weather?> = homeFragmentViewModel.weather.observeAsState()
    val weather: Weather? = weatherState.value
    val isLoading by homeFragmentViewModel.isLoading.observeAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading ?: true,
        onRefresh = {
            homeFragmentViewModel.fetchLocationLiveData().value?.let {
                homeFragmentViewModel.refreshWeather(it)
            }
        })

    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Text(
                    text = weather?.name ?: "",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_8)))
                Text(
                    text = homeFragmentViewModel.time,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_8)))
                WeatherIcon(
                    weatherDescription = weather?.networkWeatherDescription?.get(0)?.description
                        ?: ""
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Text(
                    text = stringResource(
                        tempValueResId,
                        weather?.networkWeatherCondition?.temp ?: 0.0
                    ),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Text(
                    text = weather?.networkWeatherDescription?.get(0)?.main ?: "",
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Divider(
                    modifier = Modifier.width(dimensionResource(id = R.dimen.size_25)),
                    color = Color(0xFF808080)
                )
            }
            Spacer(modifier = Modifier.weight(1F))
            BottomRow(weather = weather, modifier = Modifier.fillMaxWidth())
        }
        PullRefreshIndicator(
            refreshing = isLoading ?: true,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun WeatherIcon(weatherDescription: String, modifier: Modifier = Modifier) {
    val iconSize = integerResource(id = R.integer.home_weather_icon_size)
    val iconColor = MaterialTheme.colorScheme.secondary.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WeatherIconView(context).apply {
                setIconSize(iconSize)
                setIconColor(iconColor)
            }
        },
        update = { iconView ->
            WeatherIconGenerator.getIconResources(iconView.context, iconView, weatherDescription)
        }
    )
}

@Composable
fun BottomRow(weather: Weather?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(dimensionResource(id = R.dimen.size_16)),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomRowItem(
            image = painterResource(R.drawable.ic_humidity),
            description = stringResource(id = R.string.humidity),
            value = stringResource(
                id = R.string.humidity_value,
                weather?.networkWeatherCondition?.humidity ?: 0.0
            )
        )
        BottomRowItem(
            image = painterResource(R.drawable.ic_pressure),
            description = stringResource(id = R.string.pressure),
            value = stringResource(
                id = R.string.pressure_value,
                weather?.networkWeatherCondition?.pressure ?: 0.0
            )
        )
        BottomRowItem(
            image = painterResource(R.drawable.ic_wind),
            description = stringResource(id = R.string.wind_speed),
            value = stringResource(id = R.string.wind_speed_value, weather?.wind?.speed ?: 0.0)
        )
    }
}

@Composable
fun BottomRowItem(
    image: Painter,
    description: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(
            painter = image,
            contentDescription = description,
            modifier = Modifier.height(dimensionResource(id = R.dimen.size_30)),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_24)))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_5)))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_32)))
    }
}
