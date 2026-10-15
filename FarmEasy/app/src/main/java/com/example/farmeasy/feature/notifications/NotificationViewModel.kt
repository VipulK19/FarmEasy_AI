package com.example.farmeasy.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.AppNotification
import com.example.farmeasy.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<AppNotification> = emptyList()
)

sealed interface NotificationUiEvent {
    data class MarkAsRead(val id: String) : NotificationUiEvent
    object ClearAll : NotificationUiEvent
}

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getNotifications().collect { list ->
                _uiState.update { it.copy(notifications = list) }
            }
        }
    }

    fun onEvent(event: NotificationUiEvent) {
        when (event) {
            is NotificationUiEvent.MarkAsRead -> viewModelScope.launch { repository.markAsRead(event.id) }
            is NotificationUiEvent.ClearAll -> viewModelScope.launch { repository.clearAll() }
        }
    }
}
