package com.example.farmeasy.feature.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.core.common.AudioSpeechManager
import com.example.farmeasy.domain.repository.PreferencesRepository
import com.example.farmeasy.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceUiState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val assistantReply: String = "",
    val selectedLanguage: String = "English",
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null
)

sealed interface VoiceUiEvent {
    object StartListening : VoiceUiEvent
    object StopListening : VoiceUiEvent
    data class SpokenResultReceived(val text: String) : VoiceUiEvent
    object StopTts : VoiceUiEvent
    object DismissError : VoiceUiEvent
}

class VoiceViewModel(
    private val audioSpeechManager: AudioSpeechManager,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioSpeechManager.isListening.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }

        viewModelScope.launch {
            preferencesRepository.getPreferences().collect { prefs ->
                _uiState.update { it.copy(selectedLanguage = prefs.selectedLanguage) }
            }
        }
    }

    fun onEvent(event: VoiceUiEvent) {
        when (event) {
            is VoiceUiEvent.StartListening -> startVoiceInput()
            is VoiceUiEvent.StopListening -> audioSpeechManager.stopListening()
            is VoiceUiEvent.SpokenResultReceived -> processSpokenQuery(event.text)
            is VoiceUiEvent.StopTts -> {
                audioSpeechManager.stopSpeaking()
                _uiState.update { it.copy(isSpeaking = false) }
            }
            is VoiceUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun startVoiceInput() {
        val langCode = when (_uiState.value.selectedLanguage.lowercase()) {
            "hindi" -> "hi-IN"
            "marathi" -> "mr-IN"
            else -> "en-IN"
        }
        audioSpeechManager.stopSpeaking()
        audioSpeechManager.startListening(langCode) { recognized ->
            processSpokenQuery(recognized)
        }
    }

    private fun processSpokenQuery(text: String) {
        _uiState.update { it.copy(spokenText = text, isProcessing = true, errorMessage = null) }
        viewModelScope.launch {
            val res = sendChatMessageUseCase.send(text, _uiState.value.selectedLanguage)
            if (res.isSuccess) {
                val reply = res.getOrNull()?.text ?: ""
                _uiState.update { it.copy(isProcessing = false, assistantReply = reply, isSpeaking = true) }
                audioSpeechManager.speak(reply, _uiState.value.selectedLanguage)
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Voice advisory could not process query."
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSpeechManager.stopSpeaking()
        audioSpeechManager.stopListening()
    }
}
