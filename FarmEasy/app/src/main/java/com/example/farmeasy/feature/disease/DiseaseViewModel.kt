package com.example.farmeasy.feature.disease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.CropDiseaseAnalysis
import com.example.farmeasy.domain.usecase.AnalyzeDiseaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiseaseUiState(
    val selectedCrop: String = "Tomato",
    val imageUriString: String? = null,
    val isAnalyzing: Boolean = false,
    val currentAnalysis: CropDiseaseAnalysis? = null,
    val history: List<CropDiseaseAnalysis> = emptyList(),
    val errorMessage: String? = null
)

sealed interface DiseaseUiEvent {
    data class CropChanged(val crop: String) : DiseaseUiEvent
    data class ImageSelected(val uriString: String?) : DiseaseUiEvent
    object AnalyzeClicked : DiseaseUiEvent
    object ClearCurrentResult : DiseaseUiEvent
    object ClearHistory : DiseaseUiEvent
    object DismissError : DiseaseUiEvent
}

class DiseaseViewModel(
    private val analyzeDiseaseUseCase: AnalyzeDiseaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiseaseUiState())
    val uiState: StateFlow<DiseaseUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            analyzeDiseaseUseCase.getHistory().collect { list ->
                _uiState.update { it.copy(history = list) }
            }
        }
    }

    fun onEvent(event: DiseaseUiEvent) {
        when (event) {
            is DiseaseUiEvent.CropChanged -> _uiState.update { it.copy(selectedCrop = event.crop) }
            is DiseaseUiEvent.ImageSelected -> _uiState.update { it.copy(imageUriString = event.uriString, errorMessage = null) }
            is DiseaseUiEvent.AnalyzeClicked -> analyze()
            is DiseaseUiEvent.ClearCurrentResult -> _uiState.update { it.copy(currentAnalysis = null, imageUriString = null) }
            is DiseaseUiEvent.ClearHistory -> viewModelScope.launch { analyzeDiseaseUseCase.clearHistory() }
            is DiseaseUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun analyze() {
        val s = _uiState.value
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            val result = analyzeDiseaseUseCase.analyze(s.selectedCrop, s.imageUriString)
            if (result.isSuccess) {
                _uiState.update { it.copy(isAnalyzing = false, currentAnalysis = result.getOrNull()) }
            } else {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Analysis could not be completed."
                    )
                }
            }
        }
    }
}
