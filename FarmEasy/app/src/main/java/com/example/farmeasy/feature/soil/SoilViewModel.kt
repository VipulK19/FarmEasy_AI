package com.example.farmeasy.feature.soil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.SoilAssessment
import com.example.farmeasy.domain.model.SoilParameters
import com.example.farmeasy.domain.usecase.AssessSoilUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoilUiState(
    val phText: String = "6.8",
    val nitrogenText: String = "180.0",
    val phosphorusText: String = "32.0",
    val potassiumText: String = "220.0",
    val moistureText: String = "28.0",
    val soilType: String = "Medium Black Soil",
    val intendedCrop: String = "Wheat",
    val isAssessing: Boolean = false,
    val currentAssessment: SoilAssessment? = null,
    val history: List<SoilAssessment> = emptyList(),
    val errorMessage: String? = null
)

sealed interface SoilUiEvent {
    data class PhChanged(val text: String) : SoilUiEvent
    data class NitrogenChanged(val text: String) : SoilUiEvent
    data class PhosphorusChanged(val text: String) : SoilUiEvent
    data class PotassiumChanged(val text: String) : SoilUiEvent
    data class MoistureChanged(val text: String) : SoilUiEvent
    data class SoilTypeChanged(val type: String) : SoilUiEvent
    data class IntendedCropChanged(val crop: String) : SoilUiEvent
    object AssessClicked : SoilUiEvent
    object ClearCurrentAssessment : SoilUiEvent
    object ClearHistory : SoilUiEvent
    object DismissError : SoilUiEvent
}

class SoilViewModel(
    private val assessSoilUseCase: AssessSoilUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoilUiState())
    val uiState: StateFlow<SoilUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            assessSoilUseCase.getHistory().collect { list ->
                _uiState.update { it.copy(history = list) }
            }
        }
    }

    fun onEvent(event: SoilUiEvent) {
        when (event) {
            is SoilUiEvent.PhChanged -> _uiState.update { it.copy(phText = event.text, errorMessage = null) }
            is SoilUiEvent.NitrogenChanged -> _uiState.update { it.copy(nitrogenText = event.text, errorMessage = null) }
            is SoilUiEvent.PhosphorusChanged -> _uiState.update { it.copy(phosphorusText = event.text, errorMessage = null) }
            is SoilUiEvent.PotassiumChanged -> _uiState.update { it.copy(potassiumText = event.text, errorMessage = null) }
            is SoilUiEvent.MoistureChanged -> _uiState.update { it.copy(moistureText = event.text, errorMessage = null) }
            is SoilUiEvent.SoilTypeChanged -> _uiState.update { it.copy(soilType = event.type) }
            is SoilUiEvent.IntendedCropChanged -> _uiState.update { it.copy(intendedCrop = event.crop) }
            is SoilUiEvent.AssessClicked -> assess()
            is SoilUiEvent.ClearCurrentAssessment -> _uiState.update { it.copy(currentAssessment = null) }
            is SoilUiEvent.ClearHistory -> viewModelScope.launch { assessSoilUseCase.clearHistory() }
            is SoilUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun assess() {
        val s = _uiState.value
        val ph = s.phText.toDoubleOrNull()
        val n = s.nitrogenText.toDoubleOrNull()
        val p = s.phosphorusText.toDoubleOrNull()
        val k = s.potassiumText.toDoubleOrNull()
        val moisture = s.moistureText.toDoubleOrNull() ?: 25.0

        if (ph == null || ph !in 3.0..10.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid soil pH between 3.0 and 10.0.") }
            return
        }
        if (n == null || n < 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive value for Nitrogen (N).") }
            return
        }
        if (p == null || p < 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive value for Phosphorus (P).") }
            return
        }
        if (k == null || k < 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive value for Potassium (K).") }
            return
        }

        val params = SoilParameters(
            ph = ph,
            nitrogen = n,
            phosphorus = p,
            potassium = k,
            moisturePercent = moisture,
            soilType = s.soilType,
            intendedCrop = s.intendedCrop
        )

        _uiState.update { it.copy(isAssessing = true, errorMessage = null) }

        viewModelScope.launch {
            val result = assessSoilUseCase.assess(params)
            if (result.isSuccess) {
                _uiState.update { it.copy(isAssessing = false, currentAssessment = result.getOrNull()) }
            } else {
                _uiState.update {
                    it.copy(
                        isAssessing = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Soil assessment failed."
                    )
                }
            }
        }
    }
}
