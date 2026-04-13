package com.mayokunadeniyi.instantweather.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
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
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.model.Wind
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme
import com.mayokunadeniyi.instantweather.utils.GpsUtil
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator
import com.mayokunadeniyi.instantweather.utils.convertCelsiusToFahrenheit
import com.mayokunadeniyi.instantweather.worker.UpdateWeatherWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val location by viewModel.locationUpdates.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { SharedPreferenceHelper.getInstance(context) }
    val gpsUtil = remember { GpsUtil(context) }
    val workManager = remember { WorkManager.getInstance(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isGpsEnabled by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.refreshLocation()
            gpsUtil.turnGPSOn(object : GpsUtil.OnGpsListener {
                override fun gpsStatus(isGPSEnabled: Boolean) {
                    isGpsEnabled = isGPSEnabled
                }
            })
        }
    }

    LaunchedEffect(location, hasLocationPermission) {
        if (hasLocationPermission && location != null) {
            viewModel.onEvent(HomeUiEvent.GetWeather(location!!))
            
            // Setup WorkManager for periodic background updates
            prefs.saveLocation(location!!)
            val constraint = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val weatherUpdateRequest =
                PeriodicWorkRequestBuilder<UpdateWeatherWorker>(6, TimeUnit.HOURS)
                    .setConstraints(constraint)
                    .setInitialDelay(6, TimeUnit.HOURS)
                    .build()

            workManager.enqueueUniquePeriodicWork(
                "Update_weather_worker",
                ExistingPeriodicWorkPolicy.KEEP, weatherUpdateRequest
            )
        }
    }

    LaunchedEffect(uiState.weather) {
        uiState.weather?.let {
            prefs.saveCityId(it.cityId)
        }
    }

    HomeScreenContent(
        uiState = uiState,
        hasLocationPermission = hasLocationPermission,
        isGpsEnabled = isGpsEnabled,
        currentTime = viewModel.time,
        tempUnit = prefs.getSelectedTemperatureUnit() ?: stringResource(R.string.temp_unit_celsius),
        onRefresh = {
            location?.let { viewModel.onEvent(HomeUiEvent.RefreshWeather(it)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    currentTime: String,
    tempUnit: String,
    onRefresh: () -> Unit
) {
    if (!hasLocationPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.access_location_message),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading && uiState.weather == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.loading_text))
                    }
                }

                !uiState.dataFetchState && uiState.weather == null -> {
                    Text(
                        text = stringResource(R.string.error_occurred),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }

                uiState.weather != null -> {
                    val weather = uiState.weather
                    val isFahrenheit = tempUnit == stringResource(R.string.temp_unit_fahrenheit)
                    val displayWeather = if (isFahrenheit) {
                        weather.copy(
                            networkWeatherCondition = weather.networkWeatherCondition.copy(
                                temp = convertCelsiusToFahrenheit(weather.networkWeatherCondition.temp)
                            )
                        )
                    } else {
                        weather
                    }
                    val tempSymbol = if (isFahrenheit) {
                        stringResource(R.string.temp_symbol_fahrenheit)
                    } else {
                        stringResource(R.string.temp_symbol_celsius)
                    }

                    WeatherContent(
                        weather = displayWeather,
                        time = currentTime,
                        tempSymbol = tempSymbol
                    )
                }

                else -> {
                    if (!isGpsEnabled) {
                        Text(
                            text = stringResource(R.string.gps_required_message),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = stringResource(R.string.loading_text))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: Weather, time: String, tempSymbol: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = time,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            val weatherDescription = weather.networkWeatherDescription.firstOrNull()?.description
            val weatherIconColor = MaterialTheme.colorScheme.secondary.toArgb()

            AndroidView(
                factory = { ctx ->
                    WeatherIconView(ctx).apply {
                        setIconSize(100)
                        setIconColor(weatherIconColor)
                        WeatherIconGenerator.getIconResources(ctx, this, weatherDescription)
                    }
                },
                update = { view ->
                    WeatherIconGenerator.getIconResources(view.context, view, weatherDescription)
                },
            )

            Text(
                text = "${weather.networkWeatherCondition.temp}$tempSymbol",
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp),
            )

            weather.networkWeatherDescription.firstOrNull()?.main?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(25.dp)
                        .padding(vertical = 16.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherDetailItem(
                icon = R.drawable.ic_humidity,
                label = stringResource(R.string.humidity),
                value = "${weather.networkWeatherCondition.humidity}${stringResource(R.string.humidity_symbol)}"
            )
            WeatherDetailItem(
                icon = R.drawable.ic_pressure,
                label = stringResource(R.string.pressure),
                value = "${weather.networkWeatherCondition.pressure}${stringResource(R.string.pressure_symbol)}"
            )
            WeatherDetailItem(
                icon = R.drawable.ic_wind,
                label = stringResource(R.string.wind_speed),
                value = "${weather.wind.speed}${stringResource(R.string.wind_speed_symbol)}"
            )
        }
    }
}

@Composable
private fun WeatherDetailItem(icon: Int, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(30.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 24.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    InstantWeatherTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                weather = Weather(
                    uId = 1,
                    cityId = 1,
                    name = "Lagos",
                    wind = Wind(speed = 5.0, deg = 10),
                    networkWeatherDescription = listOf(
                        NetworkWeatherDescription(
                            id = 1,
                            main = "Clouds",
                            description = "broken clouds",
                            icon = "04d"
                        )
                    ),
                    networkWeatherCondition = NetworkWeatherCondition(
                        temp = 30.0,
                        pressure = 1012.0,
                        humidity = 70.0
                    )
                ),
                dataFetchState = true
            ),
            hasLocationPermission = true,
            isGpsEnabled = true,
            currentTime = "Monday Mar 15, 12:45 PM",
            tempUnit = "Celsius",
            onRefresh = {}
        )
    }
}
