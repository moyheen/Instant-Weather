package com.mayokunadeniyi.instantweather.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.compose.searchbox.SearchBoxState
import com.github.pwittchen.weathericonview.WeatherIconView
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.data.model.SearchResult
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.ui.theme.InstantWeatherTheme
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.WeatherIconGenerator
import com.mayokunadeniyi.instantweather.utils.convertCelsiusToFahrenheit
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagingHits = viewModel.hitsPaginator.flow.collectAsLazyPagingItems()
    val context = LocalContext.current
    val sharedPrefs = remember { SharedPreferenceHelper.getInstance(context) }

    SearchScreenContent(
        uiState = uiState,
        searchBoxState = viewModel.searchBoxState,
        pagingHits = pagingHits,
        tempUnit = sharedPrefs.getSelectedTemperatureUnit()
            ?: stringResource(R.string.temp_unit_celsius),
        onSearchResultClick = { name ->
            viewModel.onEvent(SearchUiEvent.GetSearchWeather(name))
        },
        onDismissWeatherDetail = {
            viewModel.onEvent(SearchUiEvent.DismissWeatherDetail)
        },
        onSearchSubmit = { query ->
            viewModel.onEvent(SearchUiEvent.GetSearchWeather(query))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    searchBoxState: SearchBoxState,
    pagingHits: LazyPagingItems<SearchResult>?,
    tempUnit: String,
    onSearchResultClick: (String) -> Unit,
    onDismissWeatherDetail: () -> Unit,
    onSearchSubmit: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.search_error)

    LaunchedEffect(uiState.weatherInfo) {
        if (uiState.weatherInfo != null) {
            showSheet = true
        }
    }

    LaunchedEffect(uiState.dataFetchState, uiState.isLoading) {
        if (!uiState.dataFetchState && !uiState.isLoading) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBox(
                state = searchBoxState,
                onSearchSubmit = onSearchSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                val isPagingLoading = pagingHits?.loadState?.refresh is LoadState.Loading
                val isPagingError = pagingHits?.loadState?.refresh is LoadState.Error

                when {
                    isPagingLoading && pagingHits?.itemCount == 0 -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    isPagingError -> {
                        Text(
                            text = stringResource(R.string.error_occurred),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = Color.Red
                        )
                    }

                    searchBoxState.query.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.enter_city_text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    pagingHits?.itemCount == 0 && !isPagingLoading -> {
                        Text(
                            text = stringResource(R.string.zero_hits_text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    pagingHits != null -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(pagingHits.itemCount) { index ->
                                val item = pagingHits[index] ?: return@items
                                SearchResultItem(item = item) {
                                    onSearchResultClick(item.name)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet && uiState.weatherInfo != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onDismissWeatherDetail()
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            WeatherDetailContent(
                weather = uiState.weatherInfo,
                tempUnit = tempUnit
            )
        }
    }
}

@Composable
private fun SearchBox(
    state: SearchBoxState,
    onSearchSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = state.query,
        onValueChange = { state.setText(it) },
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.enter_city_text)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (state.query.isNotEmpty()) onSearchSubmit(state.query)
        }),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun SearchResultItem(
    item: SearchResult,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${item.subcountry}, ${item.country}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun WeatherDetailContent(
    weather: Weather,
    tempUnit: String
) {
    val isFahrenheit = tempUnit == stringResource(R.string.temp_unit_fahrenheit)

    val displayTemp = if (isFahrenheit) {
        convertCelsiusToFahrenheit(weather.networkWeatherCondition.temp)
    } else {
        weather.networkWeatherCondition.temp
    }

    val tempSymbol =
        if (isFahrenheit) stringResource(R.string.temp_symbol_fahrenheit) else stringResource(R.string.temp_symbol_celsius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weather.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_cloud),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 150.dp, height = 176.dp)
                    .align(Alignment.TopStart)
            )

            val weatherDescription = weather.networkWeatherDescription.firstOrNull()?.description
            val weatherIconColor = MaterialTheme.colorScheme.onTertiary.toArgb()

            AndroidView(
                modifier = Modifier.align(Alignment.Center),
                factory = { ctx ->
                    WeatherIconView(ctx).apply {
                        setIconSize(70)
                        setIconColor(weatherIconColor)
                        WeatherIconGenerator.getIconResources(ctx, this, weatherDescription)
                    }
                },
                update = { view ->
                    WeatherIconGenerator.getIconResources(view.context, view, weatherDescription)
                },
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_cloud),
                contentDescription = null,
                modifier = Modifier
                    .size(176.dp)
                    .align(Alignment.TopEnd)
            )
        }

        Text(
            text = "${displayTemp}$tempSymbol",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        weather.networkWeatherDescription.firstOrNull()?.main?.let {
            Text(
                text = it,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherDetailItem(
                icon = R.drawable.ic_humidity,
                label = stringResource(R.string.humidity),
                value = "${weather.networkWeatherCondition.humidity}${stringResource(R.string.humidity_symbol)}"
            )
            WeatherDetailItem(
                icon = R.drawable.ic_pressure,
                label = stringResource(R.string.pressure),
                value = "${weather.networkWeatherCondition.pressure} ${stringResource(R.string.pressure_symbol)}"
            )
            WeatherDetailItem(
                icon = R.drawable.ic_wind,
                label = stringResource(R.string.wind_speed),
                value = "${weather.wind.speed} ${stringResource(R.string.wind_speed_symbol)}"
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun WeatherDetailItem(icon: Int, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(30.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiary)
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 24.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    val mockResults = listOf(
        SearchResult("Lagos", "Lagos", "Nigeria"),
        SearchResult("London", "Greater London", "United Kingdom")
    )
    val pagingData = flowOf(PagingData.from(mockResults)).collectAsLazyPagingItems()

    InstantWeatherTheme {
        SearchScreenContent(
            uiState = SearchUiState(isLoading = false),
            searchBoxState = SearchBoxState("Lagos"),
            pagingHits = pagingData,
            tempUnit = "Celsius",
            onSearchResultClick = {},
            onDismissWeatherDetail = {},
            onSearchSubmit = {}
        )
    }
}
