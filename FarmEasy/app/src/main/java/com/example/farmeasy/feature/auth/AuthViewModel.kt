package com.example.farmeasy.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.Farmer
import com.example.farmeasy.domain.usecase.LoginDemoUseCase
import com.example.farmeasy.domain.usecase.LoginUseCase
import com.example.farmeasy.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val identifier: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val state: String = "Maharashtra",
    val district: String = "Pune",
    val preferredLanguage: String = "English",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

sealed interface AuthUiEvent {
    data class IdentifierChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data class NameChanged(val value: String) : AuthUiEvent
    data class PhoneChanged(val value: String) : AuthUiEvent
    data class StateChanged(val value: String) : AuthUiEvent
    data class DistrictChanged(val value: String) : AuthUiEvent
    data class LanguageChanged(val value: String) : AuthUiEvent
    object LoginSubmitted : AuthUiEvent
    object RegisterSubmitted : AuthUiEvent
    object DemoLoginClicked : AuthUiEvent
    object DismissError : AuthUiEvent
}

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginDemoUseCase: LoginDemoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.IdentifierChanged -> _uiState.update { it.copy(identifier = event.value, errorMessage = null) }
            is AuthUiEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, errorMessage = null) }
            is AuthUiEvent.NameChanged -> _uiState.update { it.copy(name = event.value, errorMessage = null) }
            is AuthUiEvent.PhoneChanged -> _uiState.update { it.copy(phone = event.value, errorMessage = null) }
            is AuthUiEvent.StateChanged -> _uiState.update { it.copy(state = event.value) }
            is AuthUiEvent.DistrictChanged -> _uiState.update { it.copy(district = event.value) }
            is AuthUiEvent.LanguageChanged -> _uiState.update { it.copy(preferredLanguage = event.value) }
            is AuthUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }

            is AuthUiEvent.LoginSubmitted -> login()
            is AuthUiEvent.RegisterSubmitted -> register()
            is AuthUiEvent.DemoLoginClicked -> demoLogin()
        }
    }

    private fun login() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginUseCase(s.identifier, s.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Login failed.") }
            }
        }
    }

    private fun register() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val farmer = Farmer(
                name = s.name,
                phone = s.phone,
                state = s.state,
                district = s.district,
                preferredLanguage = s.preferredLanguage
            )
            val result = registerUseCase(farmer, s.password)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Registration failed.") }
            }
        }
    }

    private fun demoLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loginDemoUseCase()
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
