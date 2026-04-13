package com.mayokunadeniyi.instantweather.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.compose.searchbox.SearchBoxState
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searchbox.connectView
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.instantsearch.stats.StatsConnector
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.mayokunadeniyi.instantweather.BuildConfig
import com.mayokunadeniyi.instantweather.data.model.SearchResult
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.utils.Result
import com.mayokunadeniyi.instantweather.utils.convertKelvinToCelsius
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Mayokun Adeniyi on 27/04/2020.
 */

data class SearchUiState(
    val isLoading: Boolean = false,
    val weatherInfo: Weather? = null,
    val dataFetchState: Boolean = true
)

sealed class SearchUiEvent {
    data class GetSearchWeather(val name: String) : SearchUiEvent()
    object DismissWeatherDetail : SearchUiEvent()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val applicationID = BuildConfig.ALGOLIA_APP_ID
    private val algoliaAPIKey = BuildConfig.ALGOLIA_API_KEY
    private val algoliaIndexName = BuildConfig.ALGOLIA_INDEX_NAME

    private val client = ClientSearch(
        ApplicationID(applicationID),
        APIKey(algoliaAPIKey)
    )

    val searcher = HitsSearcher(
        client = client,
        indexName = IndexName(algoliaIndexName)
    )

    // Search Box
    val searchBoxState = SearchBoxState()
    val searchBoxConnector = SearchBoxConnector(searcher)

    // Paging
    val hitsPaginator = Paginator(searcher) { hit ->
        hit.deserialize(SearchResult.serializer())
    }

    // Stats
    val statsConnector = StatsConnector(searcher)

    private val connections = ConnectionHandler()

    init {
        connections += statsConnector
        connections += searchBoxConnector
        connections += searchBoxConnector.connectPaginator(hitsPaginator)
        connections += searchBoxConnector.connectView(searchBoxState)
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.GetSearchWeather -> getSearchWeather(event.name)
            is SearchUiEvent.DismissWeatherDetail -> {
                _uiState.update { it.copy(weatherInfo = null) }
            }
        }
    }

    private fun getSearchWeather(name: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getSearchWeather(name)) {
                is Result.Success -> {
                    if (result.data != null) {
                        Timber.i("Search Result: ${result.data}")
                        val weather = result.data.apply {
                            this.networkWeatherCondition.temp =
                                convertKelvinToCelsius(this.networkWeatherCondition.temp)
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                dataFetchState = true,
                                weatherInfo = weather
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                dataFetchState = false,
                                weatherInfo = null
                            )
                        }
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dataFetchState = false
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connections.disconnect()
    }
}
