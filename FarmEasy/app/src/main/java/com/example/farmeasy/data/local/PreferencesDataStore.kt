package com.example.farmeasy.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.farmeasy.BuildConfig
import com.example.farmeasy.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PreferencesDataStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("farmeasy_prefs", Context.MODE_PRIVATE)

    private val _prefsFlow = MutableStateFlow(readPreferences())
    val preferencesFlow: Flow<UserPreferences> = _prefsFlow

    private fun readPreferences(): UserPreferences {
        return UserPreferences(
            isDarkMode = prefs.getBoolean("dark_mode", false),
            selectedLanguage = prefs.getString("language", "English") ?: "English",
            isVoiceEnabled = prefs.getBoolean("voice_enabled", true),
            isDemoModeActive = prefs.getBoolean("demo_mode", true),
            supabaseUrl = prefs.getString("supabase_url", "") ?: "",
            supabaseAnonKey = prefs.getString("supabase_anon_key", "") ?: "",
            groqApiKey = prefs.getString("groq_api_key", BuildConfig.GROQ_API_KEY) ?: BuildConfig.GROQ_API_KEY
        )
    }

    private fun updateAndEmit(updater: (UserPreferences) -> UserPreferences) {
        val current = _prefsFlow.value
        val updated = updater(current)
        prefs.edit().apply {
            putBoolean("dark_mode", updated.isDarkMode)
            putString("language", updated.selectedLanguage)
            putBoolean("voice_enabled", updated.isVoiceEnabled)
            putBoolean("demo_mode", updated.isDemoModeActive)
            putString("supabase_url", updated.supabaseUrl)
            putString("supabase_anon_key", updated.supabaseAnonKey)
            putString("groq_api_key", updated.groqApiKey)
            apply()
        }
        _prefsFlow.value = updated
    }

    fun setDarkMode(isDark: Boolean) = updateAndEmit { it.copy(isDarkMode = isDark) }
    fun setLanguage(language: String) = updateAndEmit { it.copy(selectedLanguage = language) }
    fun setVoiceEnabled(enabled: Boolean) = updateAndEmit { it.copy(isVoiceEnabled = enabled) }
    fun setDemoMode(isDemo: Boolean) = updateAndEmit { it.copy(isDemoModeActive = isDemo) }
    fun setSupabaseConfig(url: String, anonKey: String) = updateAndEmit {
        it.copy(supabaseUrl = url, supabaseAnonKey = anonKey)
    }
    fun setGroqApiKey(key: String) = updateAndEmit { it.copy(groqApiKey = key) }
}
