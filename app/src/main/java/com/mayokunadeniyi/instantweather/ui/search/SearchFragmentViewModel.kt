package com.mayokunadeniyi.instantweather.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.stats.StatsConnector
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.mayokunadeniyi.instantweather.BuildConfig
import com.mayokunadeniyi.instantweather.data.model.SearchResult
import com.mayokunadeniyi.instantweather.data.model.Weather
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
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
}

class SearchFragmentViewModel @Inject constructor(private val repository: WeatherRepository) :
    ViewModel() {

    private val applicationID = BuildConfig.ALGOLIA_APP_ID
    private val algoliaAPIKey = BuildConfig.ALGOLIA_API_KEY
    private val algoliaIndexName = BuildConfig.ALGOLIA_INDEX_NAME
    private val client = ClientSearch(
        ApplicationID(applicationID),
        APIKey(algoliaAPIKey)
    )
    private val index = client.initIndex(IndexName(algoliaIndexName))
    private val searcher = SearcherSingleIndex(index)

    private val dataSourceFactory = SearcherSingleIndexDataSource.Factory(searcher) { hit ->
        SearchResult(
            name = hit["name"]?.jsonPrimitive?.content ?: "",
            subcountry = hit["subcountry"]?.jsonPrimitive?.content ?: "",
            country = hit["country"]?.jsonPrimitive?.content ?: ""
        )
    }

    private val pagedListConfig = PagedList.Config.Builder().setPageSize(50).build()
    val locations: LiveData<PagedList<SearchResult>> =
        LivePagedListBuilder(dataSourceFactory, pagedListConfig).build()

    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(locations))
    val stats = StatsConnector(searcher)
    private val connection = ConnectionHandler()

    init {
        connection += searchBox
        connection += stats
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.GetSearchWeather -> getSearchWeather(event.name)
        }
    }

    /**
     * Gets the [Weather] information for the user selected location[name]
     * @param name value of the location whose [Weather] data is to be fetched.
     */
    private fun getSearchWeather(name: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.getSearchWeather(name)) {
                is Result.Success -> {
                    if (result.data != null) {
                        Timber.i("Mayokun Result ${result.data}")
                        _uiState.update { it.copy(
                            isLoading = false,
                            dataFetchState = true,
                            weatherInfo = result.data
                        ) }
                    } else {
                        _uiState.update { it.copy(
                            isLoading = false,
                            dataFetchState = false,
                            weatherInfo = null
                        ) }
                    }
                }
                else -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        dataFetchState = false
                    ) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
        connection.disconnect()
    }
}
