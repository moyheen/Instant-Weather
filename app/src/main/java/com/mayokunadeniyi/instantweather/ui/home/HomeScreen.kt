package com.mayokunadeniyi.instantweather.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.NetworkWeatherCondition
import com.mayokunadeniyi.instantweather.data.model.NetworkWeatherDescription
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.model.Wind
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme

@Composable
fun HomeScreen(
    homeFragmentViewModel: HomeFragmentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    when (val homeScreenUiState = homeFragmentViewModel.homeScreenUiState) {
        is HomeScreenUiState.Success -> WeatherDetailsScreen(
            homeScreenUiState.weather,
            homeScreenUiState.selectedTemperatureUnit,
            homeScreenUiState.time,
            homeFragmentViewModel::onPullToRefresh,
            modifier = modifier.fillMaxWidth()
        )

        is HomeScreenUiState.Error -> HomeErrorScreen(modifier = modifier.fillMaxSize())
        is HomeScreenUiState.Loading -> HomeLoadingScreen(modifier = modifier.fillMaxSize())
    }
}

@Composable
fun HomeLoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.loading_text),
            color = colorScheme.onPrimary,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(
                    vertical = dimensionResource(id = R.dimen.size_24),
                    horizontal = dimensionResource(id = R.dimen.size_16)
                )
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun HomeErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.error_occurred),
            color = colorScheme.onPrimary,
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.size_16))
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    InstantWeatherTheme {
        HomeLoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    InstantWeatherTheme {
        HomeErrorScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherDetailsScreenPreview() {
    InstantWeatherTheme {
        WeatherDetailsScreen(
            Weather(
                0,
                0,
                "London",
                Wind(2.0, 20),
                listOf(NetworkWeatherDescription(0, "Clouds", "broken clouds", "04n")),
                NetworkWeatherCondition(19.28, 1001.0, 89.0)
            ),
            R.string.temp_value_fahrenheit,
            "Thursday Jul 27, 11:21 PM",
            {}
        )
    }
}
