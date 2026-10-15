package com.example.farmeasy.feature.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.WeatherReport
import com.example.farmeasy.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val selectedLocationName: String = "Pune (Main Farm)",
    val latitude: Double = 18.5204,
    val longitude: Double = 73.8567,
    val weather: WeatherReport = WeatherReport(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface WeatherUiEvent {
    data class LocationSelected(val name: String, val lat: Double, val lng: Double) : WeatherUiEvent
    object Refresh : WeatherUiEvent
    object DismissError : WeatherUiEvent
}

class WeatherViewModel(
    private val getWeatherUseCase: GetWeatherUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        observeWeather()
        refreshWeather(18.5204, 73.8567)
    }

    private fun observeWeather() {
        viewModelScope.launch {
            getWeatherUseCase().collect { report ->
                _uiState.update { it.copy(weather = report) }
            }
        }
    }

    fun onEvent(event: WeatherUiEvent) {
        when (event) {
            is WeatherUiEvent.LocationSelected -> {
                _uiState.update {
                    it.copy(
                        selectedLocationName = event.name,
                        latitude = event.lat,
                        longitude = event.lng
                    )
                }
                refreshWeather(event.lat, event.lng)
            }
            is WeatherUiEvent.Refresh -> refreshWeather(_uiState.value.latitude, _uiState.value.longitude)
            is WeatherUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun refreshWeather(lat: Double, lng: Double) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = getWeatherUseCase.refresh(lat, lng)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, weather = result.getOrNull() ?: it.weather) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not refresh fresh weather. Showing cached offline data."
                    )
                }
            }
        }
    }
}
