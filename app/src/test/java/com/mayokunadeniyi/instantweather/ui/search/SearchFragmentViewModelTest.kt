package com.mayokunadeniyi.instantweather.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mayokunadeniyi.instantweather.MainCoroutineRule
import com.mayokunadeniyi.instantweather.data.source.repository.WeatherRepository
import com.mayokunadeniyi.instantweather.fakeWeather
import com.mayokunadeniyi.instantweather.getOrAwaitValue
import com.mayokunadeniyi.instantweather.invalidDataException
import com.mayokunadeniyi.instantweather.queryLocation
import com.mayokunadeniyi.instantweather.utils.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Mayokun Adeniyi on 07/08/2020.
 */
@RunWith(MockitoJUnitRunner::class)
@ExperimentalCoroutinesApi
class SearchFragmentViewModelTest {

    //region constants

    //endregion constants

    //region helper fields
    @Mock
    private lateinit var repository: WeatherRepository
    //endregion helper fields

    private lateinit var systemUnderTest: SearchFragmentViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        systemUnderTest = SearchFragmentViewModel(repository)
    }

    @Test
    fun `assert that getSearchWeather returns the weather result successfully from the repository`() =
        mainCoroutineRule.runBlockingTest {
            `when`(repository.getSearchWeather(queryLocation)).thenReturn(
                Result.Success(
                    fakeWeather
                )
            )

            systemUnderTest.onEvent(SearchUiEvent.GetSearchWeather(queryLocation))

            verify(repository, times(1)).getSearchWeather(queryLocation)

            assertThat(systemUnderTest.uiState.value.weatherInfo, `is`(fakeWeather))
            assertThat(systemUnderTest.uiState.value.isLoading, `is`(false))
            assertThat(systemUnderTest.uiState.value.dataFetchState, `is`(true))
        }

    @Test
    fun `assert that getSearchWeather returns a null result from the repository`() =
        mainCoroutineRule.runBlockingTest {
            `when`(repository.getSearchWeather(queryLocation)).thenReturn(
                Result.Success(
                    null
                )
            )

            systemUnderTest.onEvent(SearchUiEvent.GetSearchWeather(queryLocation))

            verify(repository, times(1)).getSearchWeather(queryLocation)

            assertThat(systemUnderTest.uiState.value.weatherInfo, `is`(nullValue()))
            assertThat(systemUnderTest.uiState.value.isLoading, `is`(false))
            assertThat(systemUnderTest.uiState.value.dataFetchState, `is`(false))
        }

    @Test
    fun `assert that getSearchWeather returns an error from the repository`() =
        mainCoroutineRule.runBlockingTest {
            `when`(repository.getSearchWeather(queryLocation)).thenReturn(
                Result.Error(
                    invalidDataException
                )
            )

            systemUnderTest.onEvent(SearchUiEvent.GetSearchWeather(queryLocation))

            verify(repository, times(1)).getSearchWeather(queryLocation)

            assertThat(systemUnderTest.uiState.value.isLoading, `is`(false))
            assertThat(systemUnderTest.uiState.value.dataFetchState, `is`(false))
        }

    // region helper methods

    // endregion helper methods

    // region helper classes

    // endregion helper classes
}
