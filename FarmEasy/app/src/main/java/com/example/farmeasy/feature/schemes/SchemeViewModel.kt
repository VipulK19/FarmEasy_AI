package com.example.farmeasy.feature.schemes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.LoanInformation
import com.example.farmeasy.domain.model.Scheme
import com.example.farmeasy.domain.repository.SchemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchemeUiState(
    val searchQuery: String = "",
    val schemes: List<Scheme> = emptyList(),
    val loans: List<LoanInformation> = emptyList(),
    val selectedScheme: Scheme? = null,
    val selectedLoan: LoanInformation? = null,
    val isLoading: Boolean = false
)

sealed interface SchemeUiEvent {
    data class SearchQueryChanged(val query: String) : SchemeUiEvent
    data class SchemeSelected(val scheme: Scheme?) : SchemeUiEvent
    data class LoanSelected(val loan: LoanInformation?) : SchemeUiEvent
}

class SchemeViewModel(
    private val repository: SchemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchemeUiState())
    val uiState: StateFlow<SchemeUiState> = _uiState.asStateFlow()

    private var allSchemes = listOf<Scheme>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getSchemes().collect { list ->
                allSchemes = list
                filterSchemes(_uiState.value.searchQuery)
            }
        }

        viewModelScope.launch {
            repository.getLoans().collect { loans ->
                _uiState.update { it.copy(loans = loans) }
            }
        }
    }

    fun onEvent(event: SchemeUiEvent) {
        when (event) {
            is SchemeUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterSchemes(event.query)
            }
            is SchemeUiEvent.SchemeSelected -> _uiState.update { it.copy(selectedScheme = event.scheme) }
            is SchemeUiEvent.LoanSelected -> _uiState.update { it.copy(selectedLoan = event.loan) }
        }
    }

    private fun filterSchemes(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(schemes = allSchemes) }
        } else {
            val q = query.lowercase().trim()
            val filtered = allSchemes.filter {
                it.title.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.benefitSummary.lowercase().contains(q)
            }
            _uiState.update { it.copy(schemes = filtered) }
        }
    }
}
