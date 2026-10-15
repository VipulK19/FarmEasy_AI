package com.example.farmeasy.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.*
import com.example.farmeasy.domain.repository.AuthRepository
import com.example.farmeasy.domain.repository.DiseaseRepository
import com.example.farmeasy.domain.repository.FarmRepository
import com.example.farmeasy.domain.repository.RecommendationRepository
import com.example.farmeasy.domain.repository.SoilRepository
import com.example.farmeasy.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val farmer: Farmer? = null,
    val weather: WeatherReport = WeatherReport(),
    val farms: List<Farm> = emptyList(),
    val recommendations: List<AgriculturalRecommendation> = emptyList(),
    val latestDiseaseAnalysis: CropDiseaseAnalysis? = null,
    val latestSoilAssessment: SoilAssessment? = null,
    val isRefreshing: Boolean = false,
    val isDemoMode: Boolean = true
)

sealed interface DashboardUiEvent {
    object RefreshData : DashboardUiEvent
}

class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val farmRepository: FarmRepository,
    private val weatherRepository: WeatherRepository,
    private val recommendationRepository: RecommendationRepository,
    private val diseaseRepository: DiseaseRepository,
    private val soilRepository: SoilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.getCurrentFarmer().collect { farmer ->
                _uiState.update { it.copy(farmer = farmer, isDemoMode = farmer?.isDemoUser ?: true) }
            }
        }

        viewModelScope.launch {
            weatherRepository.getWeather().collect { weather ->
                _uiState.update { it.copy(weather = weather) }
            }
        }

        viewModelScope.launch {
            farmRepository.getFarms().collect { farms ->
                _uiState.update { it.copy(farms = farms) }
            }
        }

        viewModelScope.launch {
            recommendationRepository.getRecommendations().collect { recs ->
                _uiState.update { it.copy(recommendations = recs) }
            }
        }

        viewModelScope.launch {
            diseaseRepository.getAnalysisHistory().collect { list ->
                _uiState.update { it.copy(latestDiseaseAnalysis = list.firstOrNull()) }
            }
        }

        viewModelScope.launch {
            soilRepository.getAssessmentHistory().collect { list ->
                _uiState.update { it.copy(latestSoilAssessment = list.firstOrNull()) }
            }
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.RefreshData -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            weatherRepository.refreshWeather(18.5204, 73.8567)
            recommendationRepository.refreshRecommendations()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
