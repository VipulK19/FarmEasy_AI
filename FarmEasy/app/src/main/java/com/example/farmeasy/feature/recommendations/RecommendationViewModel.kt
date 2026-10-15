package com.example.farmeasy.feature.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.AgriculturalRecommendation
import com.example.farmeasy.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecommendationUiState(
    val selectedCategory: String = "All",
    val recommendations: List<AgriculturalRecommendation> = emptyList(),
    val isRefreshing: Boolean = false
)

sealed interface RecommendationUiEvent {
    data class CategorySelected(val category: String) : RecommendationUiEvent
    object Refresh : RecommendationUiEvent
}

class RecommendationViewModel(
    private val repository: RecommendationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationUiState())
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private var allRecommendations = listOf<AgriculturalRecommendation>()

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            repository.getRecommendations().collect { list ->
                allRecommendations = list
                applyFilter(_uiState.value.selectedCategory)
            }
        }
    }

    fun onEvent(event: RecommendationUiEvent) {
        when (event) {
            is RecommendationUiEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                applyFilter(event.category)
            }
            is RecommendationUiEvent.Refresh -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isRefreshing = true) }
                    repository.refreshRecommendations()
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }

    private fun applyFilter(category: String) {
        val filtered = if (category == "All") {
            allRecommendations
        } else {
            allRecommendations.filter { it.category.contains(category, ignoreCase = true) }
        }
        _uiState.update { it.copy(recommendations = filtered) }
    }
}
