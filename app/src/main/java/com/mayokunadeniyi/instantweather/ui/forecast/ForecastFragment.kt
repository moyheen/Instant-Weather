package com.mayokunadeniyi.instantweather.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mayokunadeniyi.instantweather.R
import com.mayokunadeniyi.instantweather.databinding.FragmentForecastBinding
import com.mayokunadeniyi.instantweather.ui.BaseFragment
import com.mayokunadeniyi.instantweather.ui.forecast.WeatherForecastAdapter.ForecastOnClickListener
import com.mayokunadeniyi.instantweather.utils.SharedPreferenceHelper
import com.mayokunadeniyi.instantweather.utils.convertCelsiusToFahrenheit
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ForecastFragment : BaseFragment(), ForecastOnClickListener {
    private lateinit var binding: FragmentForecastBinding

    private val viewModel by viewModels<ForecastFragmentViewModel> { viewModelFactoryProvider }

    private val weatherForecastAdapter by lazy { WeatherForecastAdapter(this) }

    @Inject
    lateinit var prefs: SharedPreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForecastBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        binding.forecastRecyclerview.adapter = weatherForecastAdapter
        viewModel.onEvent(ForecastUiEvent.GetWeatherForecast(prefs.getCityId()))
        observeMoreViewModels()
    }

    private fun observeMoreViewModels() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.forecast?.let { list ->
                    list.forEach {
                        if (prefs.getSelectedTemperatureUnit() == requireActivity().resources.getString(R.string.temp_unit_fahrenheit))
                            it.networkWeatherCondition.temp = convertCelsiusToFahrenheit(it.networkWeatherCondition.temp)
                    }
                    weatherForecastAdapter.submitList(list)
                }

                binding.apply {
                    forecastRecyclerview.isVisible = state.dataFetchState
                    forecastErrorText?.isVisible = !state.dataFetchState
                }

                binding.forecastProgressBar.isVisible = state.isLoading

                if (state.filteredForecast.isNotEmpty()) {
                    weatherForecastAdapter.submitList(state.filteredForecast)
                }
                binding.emptyListText.isVisible = state.filteredForecast.isEmpty() && state.forecast != null
            }
        }

        binding.forecastSwipeRefresh.setOnRefreshListener {
            initiateRefresh()
        }
    }

    private fun initiateRefresh() {
        binding.forecastErrorText?.visibility = View.GONE
        binding.forecastProgressBar.visibility = View.VISIBLE
        binding.forecastRecyclerview.visibility = View.GONE
        viewModel.onEvent(ForecastUiEvent.RefreshForecastData(prefs.getCityId()))
        binding.forecastSwipeRefresh.isRefreshing = false
    }

    private fun setupCalendar() {
        binding.calendarView.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onClickListener() {
            }

            override fun onDataUpdate() {
            }

            override fun onDayChanged() {
            }

            override fun onDaySelect() {
                binding.emptyListText.visibility = View.GONE
                runCatching {
                    val selectedDay = binding.calendarView.selectedDay
                    val list = viewModel.uiState.value.forecast
                    viewModel.onEvent(ForecastUiEvent.UpdateWeatherForecast(requireNotNull(selectedDay), requireNotNull(list)))
                }.onFailure {
                    Timber.d(it)
                }
            }

            override fun onItemClick(v: View) {
            }

            override fun onMonthChange() {
            }

            override fun onWeekChange(position: Int) {
            }
        })
    }
}
