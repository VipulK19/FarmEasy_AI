package com.example.farmeasy.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.Farmer
import com.example.farmeasy.domain.model.UserPreferences
import com.example.farmeasy.domain.repository.AuthRepository
import com.example.farmeasy.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val farmer: Farmer? = null,
    val preferences: UserPreferences = UserPreferences(),
    val showBackendConfigDialog: Boolean = false,
    val supabaseUrlInput: String = "",
    val supabaseAnonKeyInput: String = "",
    val groqApiKeyInput: String = "",
    val showLogoutConfirmation: Boolean = false,
    val isLoggedOut: Boolean = false
)

sealed interface ProfileUiEvent {
    data class LanguageChanged(val language: String) : ProfileUiEvent
    data class DarkModeToggled(val enabled: Boolean) : ProfileUiEvent
    data class VoiceToggled(val enabled: Boolean) : ProfileUiEvent
    data class DemoModeToggled(val enabled: Boolean) : ProfileUiEvent
    object ShowBackendDialog : ProfileUiEvent
    object DismissBackendDialog : ProfileUiEvent
    data class SupabaseUrlChanged(val url: String) : ProfileUiEvent
    data class SupabaseKeyChanged(val key: String) : ProfileUiEvent
    data class GroqKeyChanged(val key: String) : ProfileUiEvent
    object SaveBackendConfig : ProfileUiEvent
    object ShowLogoutDialog : ProfileUiEvent
    object DismissLogoutDialog : ProfileUiEvent
    object LogoutConfirmed : ProfileUiEvent
}

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.getCurrentFarmer().collect { farmer ->
                _uiState.update { it.copy(farmer = farmer) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.getPreferences().collect { prefs ->
                _uiState.update {
                    it.copy(
                        preferences = prefs,
                        supabaseUrlInput = prefs.supabaseUrl,
                        supabaseAnonKeyInput = prefs.supabaseAnonKey,
                        groqApiKeyInput = prefs.groqApiKey
                    )
                }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LanguageChanged -> viewModelScope.launch { preferencesRepository.updateLanguage(event.language) }
            is ProfileUiEvent.DarkModeToggled -> viewModelScope.launch { preferencesRepository.updateDarkMode(event.enabled) }
            is ProfileUiEvent.VoiceToggled -> viewModelScope.launch { preferencesRepository.updateVoiceEnabled(event.enabled) }
            is ProfileUiEvent.DemoModeToggled -> viewModelScope.launch { preferencesRepository.updateDemoMode(event.enabled) }
            is ProfileUiEvent.ShowBackendDialog -> _uiState.update { it.copy(showBackendConfigDialog = true) }
            is ProfileUiEvent.DismissBackendDialog -> _uiState.update { it.copy(showBackendConfigDialog = false) }
            is ProfileUiEvent.SupabaseUrlChanged -> _uiState.update { it.copy(supabaseUrlInput = event.url) }
            is ProfileUiEvent.SupabaseKeyChanged -> _uiState.update { it.copy(supabaseAnonKeyInput = event.key) }
            is ProfileUiEvent.GroqKeyChanged -> _uiState.update { it.copy(groqApiKeyInput = event.key) }
            is ProfileUiEvent.SaveBackendConfig -> saveBackendConfig()
            is ProfileUiEvent.ShowLogoutDialog -> _uiState.update { it.copy(showLogoutConfirmation = true) }
            is ProfileUiEvent.DismissLogoutDialog -> _uiState.update { it.copy(showLogoutConfirmation = false) }
            is ProfileUiEvent.LogoutConfirmed -> logout()
        }
    }

    private fun saveBackendConfig() {
        val s = _uiState.value
        viewModelScope.launch {
            preferencesRepository.updateSupabaseConfig(s.supabaseUrlInput.trim(), s.supabaseAnonKeyInput.trim())
            preferencesRepository.updateGroqApiKey(s.groqApiKeyInput.trim())
            _uiState.update { it.copy(showBackendConfigDialog = false) }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true, showLogoutConfirmation = false) }
        }
    }
}
