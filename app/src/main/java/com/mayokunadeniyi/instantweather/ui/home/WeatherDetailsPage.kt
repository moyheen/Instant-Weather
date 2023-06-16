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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherDetailsPage(homeFragmentViewModel: HomeFragmentViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val tempValueResId = if (SharedPreferenceHelper.getInstance(context)
            .getSelectedTemperatureUnit() == context.getString(R.string.temp_unit_fahrenheit)
    )
        R.string.temp_value_fahrenheit
    else
        R.string.temp_value_celsius

    val weatherState: State<Weather?> = homeFragmentViewModel.weather.observeAsState()
    val weather: Weather? = weatherState.value
    val isLoading by homeFragmentViewModel.isLoading.observeAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing = isLoading ?: true, onRefresh = {
        homeFragmentViewModel.fetchLocationLiveData().value?.let {
            homeFragmentViewModel.refreshWeather(it)
        }
    })

    Box(
        Modifier
            .pullRefresh(pullRefreshState)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = weather?.name ?: "",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = homeFragmentViewModel.time,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            AndroidView(::WeatherIconView) { iconView ->
                iconView.setIconSize(100)
                iconView.setIconColor(R.color.primaryLightColor)
                WeatherIconGenerator.getIconResources(
                    context,
                    iconView,
                    weather?.networkWeatherDescription?.get(0)?.description ?: ""
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(
                    tempValueResId,
                    weather?.networkWeatherCondition?.temp ?: 0.0
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = weather?.networkWeatherDescription?.get(0)?.main ?: "",
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.width(25.dp)
            ) {
                Divider(color = Color(0xFF808080))
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            RowItem(
                image = painterResource(R.drawable.ic_humidity),
                description = stringResource(id = R.string.humidity),
                value = stringResource(
                    id = R.string.humidity_value,
                    weather?.networkWeatherCondition?.humidity ?: 0.0
                )
            )
            RowItem(
                image = painterResource(R.drawable.ic_pressure),
                description = stringResource(id = R.string.pressure),
                value = stringResource(
                    id = R.string.pressure_value,
                    weather?.networkWeatherCondition?.pressure ?: 0.0
                )
            )
            RowItem(
                image = painterResource(R.drawable.ic_wind),
                description = stringResource(id = R.string.wind_speed),
                value = stringResource(id = R.string.wind_speed_value, weather?.wind?.speed ?: 0.0)
            )
        }

        PullRefreshIndicator(
            refreshing = isLoading ?: true,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun RowItem(image: Painter, description: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = image,
            contentDescription = description,
            modifier = Modifier.height(30.dp),
            colorFilter = ColorFilter.tint(colorResource(id = R.color.primaryLightColor))
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}