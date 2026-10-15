package com.example.farmeasy.feature.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.core.common.AudioSpeechManager
import com.example.farmeasy.domain.model.ChatMessage
import com.example.farmeasy.domain.repository.PreferencesRepository
import com.example.farmeasy.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputPrompt: String = "",
    val selectedLanguage: String = "English",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSpeaking: Boolean = false,
    val showClearConfirmation: Boolean = false
)

sealed interface ChatUiEvent {
    data class InputChanged(val text: String) : ChatUiEvent
    data class LanguageSelected(val language: String) : ChatUiEvent
    data class SuggestionClicked(val prompt: String) : ChatUiEvent
    data class SpeakMessage(val text: String) : ChatUiEvent
    object StopSpeaking : ChatUiEvent
    object SendMessage : ChatUiEvent
    object ShowClearDialog : ChatUiEvent
    object HideClearDialog : ChatUiEvent
    object ClearChatConfirmed : ChatUiEvent
    object DismissError : ChatUiEvent
}

class ChatViewModel(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val audioSpeechManager: AudioSpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val suggestedQuestions = listOf(
        "Why are my tomato leaves turning yellow?",
        "What is the best fertilizer dose for wheat at tillering?",
        "How to protect cotton from pink bollworm organically?",
        "What is the procedure to apply for PM-KISAN subsidy?"
    )

    init {
        loadMessagesAndPreferences()
    }

    private fun loadMessagesAndPreferences() {
        viewModelScope.launch {
            preferencesRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(selectedLanguage = prefs.selectedLanguage) }
            }
        }

        viewModelScope.launch {
            sendChatMessageUseCase.getMessages().collect { list ->
                _uiState.update { it.copy(messages = list) }
            }
        }
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.InputChanged -> _uiState.update { it.copy(inputPrompt = event.text) }
            is ChatUiEvent.LanguageSelected -> {
                _uiState.update { it.copy(selectedLanguage = event.language) }
                viewModelScope.launch { preferencesRepository.updateLanguage(event.language) }
            }
            is ChatUiEvent.SuggestionClicked -> {
                _uiState.update { it.copy(inputPrompt = event.prompt) }
                sendMessage(event.prompt)
            }
            is ChatUiEvent.SendMessage -> sendMessage(_uiState.value.inputPrompt)
            is ChatUiEvent.SpeakMessage -> {
                audioSpeechManager.speak(event.text, _uiState.value.selectedLanguage)
                _uiState.update { it.copy(isSpeaking = true) }
            }
            is ChatUiEvent.StopSpeaking -> {
                audioSpeechManager.stopSpeaking()
                _uiState.update { it.copy(isSpeaking = false) }
            }
            is ChatUiEvent.ShowClearDialog -> _uiState.update { it.copy(showClearConfirmation = true) }
            is ChatUiEvent.HideClearDialog -> _uiState.update { it.copy(showClearConfirmation = false) }
            is ChatUiEvent.ClearChatConfirmed -> clearChat()
            is ChatUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _uiState.value.isLoading) return

        val lang = _uiState.value.selectedLanguage
        _uiState.update { it.copy(inputPrompt = "", isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = sendChatMessageUseCase.send(prompt, lang)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to generate AI response."
                    )
                }
            }
        }
    }

    private fun clearChat() {
        viewModelScope.launch {
            sendChatMessageUseCase.clear()
            _uiState.update { it.copy(showClearConfirmation = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSpeechManager.stopSpeaking()
    }
}
