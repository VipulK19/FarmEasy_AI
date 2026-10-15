package com.example.farmeasy.di

import android.content.Context
import com.example.farmeasy.core.common.AudioSpeechManager
import com.example.farmeasy.core.common.NetworkMonitor
import com.example.farmeasy.core.ml.CropDiseaseClassifier
import com.example.farmeasy.core.ml.SoilRecommendationEngine
import com.example.farmeasy.data.local.FarmEasyDatabaseHelper
import com.example.farmeasy.data.local.PreferencesDataStore
import com.example.farmeasy.data.local.dao.*
import com.example.farmeasy.data.remote.KtorClientProvider
import com.example.farmeasy.data.remote.groq.GroqService
import com.example.farmeasy.data.remote.supabase.SupabaseAuthService
import com.example.farmeasy.data.remote.weather.OpenMeteoService
import com.example.farmeasy.data.repository.*
import com.example.farmeasy.domain.repository.*
import com.example.farmeasy.domain.usecase.*

interface AppContainer {
    val authRepository: AuthRepository
    val farmRepository: FarmRepository
    val diseaseRepository: DiseaseRepository
    val soilRepository: SoilRepository
    val recommendationRepository: RecommendationRepository
    val schemeRepository: SchemeRepository
    val dealerRepository: DealerRepository
    val marketplaceRepository: MarketplaceRepository
    val weatherRepository: WeatherRepository
    val chatRepository: ChatRepository
    val notificationRepository: NotificationRepository
    val preferencesRepository: PreferencesRepository

    val networkMonitor: NetworkMonitor
    val audioSpeechManager: AudioSpeechManager

    // Use cases
    val loginUseCase: LoginUseCase
    val registerUseCase: RegisterUseCase
    val getFarmerProfileUseCase: GetFarmerProfileUseCase
    val loginDemoUseCase: LoginDemoUseCase
    val manageFarmUseCase: ManageFarmUseCase
    val analyzeDiseaseUseCase: AnalyzeDiseaseUseCase
    val assessSoilUseCase: AssessSoilUseCase
    val getWeatherUseCase: GetWeatherUseCase
    val sendChatMessageUseCase: SendChatMessageUseCase
    val manageMarketplaceUseCase: ManageMarketplaceUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val dbHelper = FarmEasyDatabaseHelper(context)
    private val httpClient = KtorClientProvider.client

    // DAOs
    private val farmDao = FarmDao(dbHelper)
    private val cropDao = CropDao(dbHelper)
    private val diseaseDao = DiseaseDao(dbHelper)
    private val soilDao = SoilDao(dbHelper)
    private val cartDao = CartDao(dbHelper)
    private val chatDao = ChatDao(dbHelper)
    private val notificationDao = NotificationDao(dbHelper)
    private val preferencesDataStore = PreferencesDataStore(context)

    // Remote Services
    private val supabaseAuthService = SupabaseAuthService(httpClient)
    private val openMeteoService = OpenMeteoService(httpClient)
    private val groqService = GroqService(httpClient)

    // ML engines
    private val diseaseClassifier = CropDiseaseClassifier(context)
    private val soilEngine = SoilRecommendationEngine()

    // Core managers
    override val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context) }
    override val audioSpeechManager: AudioSpeechManager by lazy { AudioSpeechManager(context) }

    // Repositories
    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(supabaseAuthService, preferencesDataStore)
    }

    override val farmRepository: FarmRepository by lazy {
        FarmRepositoryImpl(farmDao, cropDao)
    }

    override val diseaseRepository: DiseaseRepository by lazy {
        DiseaseRepositoryImpl(diseaseClassifier, diseaseDao)
    }

    override val soilRepository: SoilRepository by lazy {
        SoilRepositoryImpl(soilEngine, soilDao)
    }

    override val recommendationRepository: RecommendationRepository by lazy {
        RecommendationRepositoryImpl()
    }

    override val schemeRepository: SchemeRepository by lazy {
        SchemeRepositoryImpl()
    }

    override val dealerRepository: DealerRepository by lazy {
        DealerRepositoryImpl()
    }

    override val marketplaceRepository: MarketplaceRepository by lazy {
        MarketplaceRepositoryImpl(cartDao)
    }

    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(openMeteoService)
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(groqService, chatDao, preferencesDataStore)
    }

    override val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(notificationDao)
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(preferencesDataStore)
    }

    // Use cases
    override val loginUseCase by lazy { LoginUseCase(authRepository) }
    override val registerUseCase by lazy { RegisterUseCase(authRepository) }
    override val getFarmerProfileUseCase by lazy { GetFarmerProfileUseCase(authRepository) }
    override val loginDemoUseCase by lazy { LoginDemoUseCase(authRepository) }
    override val manageFarmUseCase by lazy { ManageFarmUseCase(farmRepository) }
    override val analyzeDiseaseUseCase by lazy { AnalyzeDiseaseUseCase(diseaseRepository) }
    override val assessSoilUseCase by lazy { AssessSoilUseCase(soilRepository) }
    override val getWeatherUseCase by lazy { GetWeatherUseCase(weatherRepository) }
    override val sendChatMessageUseCase by lazy { SendChatMessageUseCase(chatRepository) }
    override val manageMarketplaceUseCase by lazy { ManageMarketplaceUseCase(marketplaceRepository) }
}
