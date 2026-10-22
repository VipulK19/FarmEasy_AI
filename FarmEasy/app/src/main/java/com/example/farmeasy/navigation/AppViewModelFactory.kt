package com.example.farmeasy.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.farmeasy.di.AppContainer
import com.example.farmeasy.feature.auth.AuthViewModel
import com.example.farmeasy.feature.chatbot.ChatViewModel
import com.example.farmeasy.feature.dashboard.DashboardViewModel
import com.example.farmeasy.feature.dealer.DealerViewModel
import com.example.farmeasy.feature.disease.DiseaseViewModel
import com.example.farmeasy.feature.farm.FarmViewModel
import com.example.farmeasy.feature.marketplace.MarketplaceViewModel
import com.example.farmeasy.feature.notifications.NotificationViewModel
import com.example.farmeasy.feature.profile.ProfileViewModel
import com.example.farmeasy.feature.recommendations.RecommendationViewModel
import com.example.farmeasy.feature.schemes.SchemeViewModel
import com.example.farmeasy.feature.soil.SoilViewModel
import com.example.farmeasy.feature.voice.VoiceViewModel
import com.example.farmeasy.feature.weather.WeatherViewModel

class AppViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(
                    appContainer.loginUseCase,
                    appContainer.registerUseCase,
                    appContainer.loginDemoUseCase
                ) as T

            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(
                    appContainer.authRepository,
                    appContainer.farmRepository,
                    appContainer.weatherRepository,
                    appContainer.recommendationRepository,
                    appContainer.diseaseRepository,
                    appContainer.soilRepository
                ) as T

            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(
                    appContainer.sendChatMessageUseCase,
                    appContainer.preferencesRepository,
                    appContainer.audioSpeechManager
                ) as T

            modelClass.isAssignableFrom(FarmViewModel::class.java) ->
                FarmViewModel(appContainer.manageFarmUseCase) as T

            modelClass.isAssignableFrom(DiseaseViewModel::class.java) ->
                DiseaseViewModel(appContainer.analyzeDiseaseUseCase) as T

            modelClass.isAssignableFrom(SoilViewModel::class.java) ->
                SoilViewModel(appContainer.assessSoilUseCase) as T

            modelClass.isAssignableFrom(RecommendationViewModel::class.java) ->
                RecommendationViewModel(appContainer.recommendationRepository) as T

            modelClass.isAssignableFrom(SchemeViewModel::class.java) ->
                SchemeViewModel(appContainer.schemeRepository) as T

            modelClass.isAssignableFrom(DealerViewModel::class.java) ->
                DealerViewModel(appContainer.dealerRepository) as T

            modelClass.isAssignableFrom(MarketplaceViewModel::class.java) ->
                MarketplaceViewModel(appContainer.manageMarketplaceUseCase) as T

            modelClass.isAssignableFrom(WeatherViewModel::class.java) ->
                WeatherViewModel(appContainer.getWeatherUseCase) as T

            modelClass.isAssignableFrom(VoiceViewModel::class.java) ->
                VoiceViewModel(
                    appContainer.audioSpeechManager,
                    appContainer.sendChatMessageUseCase,
                    appContainer.preferencesRepository
                ) as T

            modelClass.isAssignableFrom(NotificationViewModel::class.java) ->
                NotificationViewModel(appContainer.notificationRepository) as T

            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(
                    appContainer.authRepository,
                    appContainer.preferencesRepository
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
