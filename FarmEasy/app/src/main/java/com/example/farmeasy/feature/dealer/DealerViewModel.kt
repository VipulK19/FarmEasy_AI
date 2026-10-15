package com.example.farmeasy.feature.dealer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.Dealer
import com.example.farmeasy.domain.repository.DealerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DealerUiState(
    val searchQuery: String = "",
    val dealers: List<Dealer> = emptyList(),
    val latitude: Double = 18.5204,
    val longitude: Double = 73.8567,
    val isLoading: Boolean = false
)

sealed interface DealerUiEvent {
    data class SearchQueryChanged(val query: String) : DealerUiEvent
    data class LocationUpdated(val lat: Double, val lng: Double) : DealerUiEvent
}

class DealerViewModel(
    private val repository: DealerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DealerUiState())
    val uiState: StateFlow<DealerUiState> = _uiState.asStateFlow()

    private var allDealers = listOf<Dealer>()

    init {
        loadDealers(18.5204, 73.8567)
    }

    private fun loadDealers(lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.getNearbyDealers(lat, lng).collect { list ->
                allDealers = list
                filterDealers(_uiState.value.searchQuery)
            }
        }
    }

    fun onEvent(event: DealerUiEvent) {
        when (event) {
            is DealerUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterDealers(event.query)
            }
            is DealerUiEvent.LocationUpdated -> {
                _uiState.update { it.copy(latitude = event.lat, longitude = event.lng) }
                loadDealers(event.lat, event.lng)
            }
        }
    }

    private fun filterDealers(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(dealers = allDealers) }
        } else {
            val q = query.lowercase().trim()
            val filtered = allDealers.filter {
                it.businessName.lowercase().contains(q) ||
                        it.dealerType.lowercase().contains(q) ||
                        it.address.lowercase().contains(q)
            }
            _uiState.update { it.copy(dealers = filtered) }
        }
    }
}
