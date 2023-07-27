package com.mayokunadeniyi.instantweather.ui.home

import androidx.annotation.StringRes
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeatherDetailsScreen(
    weather: Weather,
    @StringRes tempValueResId: Int,
    time: String,
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRefreshing = rememberSaveable { false }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onPullToRefresh()
        }
    )

    Box(modifier = modifier.pullRefresh(pullRefreshState)) {
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
                    text = weather.name,
                    color = colorScheme.onPrimary,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_8)))
                Text(
                    text = time,
                    color = colorScheme.onPrimary,
                    style = typography.bodySmall
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_8)))
                WeatherIcon(
                    weatherDescription = weather.networkWeatherDescription[0].description ?: ""
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Text(
                    text = stringResource(
                        tempValueResId,
                        weather.networkWeatherCondition.temp
                    ),
                    color = colorScheme.onPrimary,
                    style = typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Text(
                    text = weather.networkWeatherDescription[0].main ?: "",
                    color = colorScheme.onTertiary,
                    style = typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_16)))
                Divider(
                    modifier = Modifier.width(dimensionResource(id = R.dimen.size_25)),
                    color = colorScheme.onTertiary
                )
            }
            Spacer(modifier = Modifier.weight(1F))
            BottomRow(weather = weather, modifier = Modifier.fillMaxWidth())
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun WeatherIcon(weatherDescription: String, modifier: Modifier = Modifier) {
    val iconSize = integerResource(id = R.integer.home_weather_icon_size)
    val iconColor = colorScheme.secondary.toArgb()

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
fun BottomRow(weather: Weather, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(dimensionResource(id = R.dimen.size_16)),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomRowItem(
            image = painterResource(R.drawable.ic_humidity),
            description = stringResource(id = R.string.humidity),
            value = stringResource(
                id = R.string.humidity_value,
                weather.networkWeatherCondition.humidity
            )
        )
        BottomRowItem(
            image = painterResource(R.drawable.ic_pressure),
            description = stringResource(id = R.string.pressure),
            value = stringResource(
                id = R.string.pressure_value,
                weather.networkWeatherCondition.pressure
            )
        )
        BottomRowItem(
            image = painterResource(R.drawable.ic_wind),
            description = stringResource(id = R.string.wind_speed),
            value = stringResource(id = R.string.wind_speed_value, weather.wind.speed)
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
            colorFilter = ColorFilter.tint(colorScheme.secondary)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_24)))
        Text(text = description, color = colorScheme.onPrimary, style = typography.bodySmall)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_5)))
        Text(text = value, color = colorScheme.onPrimary, style = typography.bodySmall)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.size_32)))
    }
}
