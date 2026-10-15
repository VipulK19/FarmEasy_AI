package com.example.farmeasy.domain.repository

import com.example.farmeasy.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentFarmer(): Flow<Farmer?>
    suspend fun login(phoneOrEmail: String, password: String):Result<Farmer>
    suspend fun register(farmer: Farmer, password: String): Result<Farmer>
    suspend fun logout(): Result<Unit>
    suspend fun loginAsDemoFarmer(): Farmer
    suspend fun updateProfile(farmer: Farmer): Result<Farmer>
}

interface FarmRepository {
    fun getFarms(): Flow<List<Farm>>
    suspend fun addFarm(farm: Farm): Result<Unit>
    suspend fun updateFarm(farm: Farm): Result<Unit>
    suspend fun deleteFarm(farmId: String): Result<Unit>
    fun getCrops(farmId: String): Flow<List<Crop>>
    suspend fun addCrop(crop: Crop): Result<Unit>
    suspend fun updateCrop(crop: Crop): Result<Unit>
    suspend fun deleteCrop(cropId: String): Result<Unit>
}

interface DiseaseRepository {
    fun getAnalysisHistory(): Flow<List<CropDiseaseAnalysis>>
    suspend fun analyzeCrop(cropName: String, imageUriString: String?): Result<CropDiseaseAnalysis>
    suspend fun clearHistory(): Result<Unit>
}

interface SoilRepository {
    fun getAssessmentHistory(): Flow<List<SoilAssessment>>
    suspend fun assessSoil(parameters: SoilParameters): Result<SoilAssessment>
    suspend fun clearHistory(): Result<Unit>
}

interface RecommendationRepository {
    fun getRecommendations(): Flow<List<AgriculturalRecommendation>>
    suspend fun refreshRecommendations(): Result<List<AgriculturalRecommendation>>
}

interface SchemeRepository {
    fun getSchemes(): Flow<List<Scheme>>
    fun getLoans(): Flow<List<LoanInformation>>
    suspend fun searchSchemes(query: String): List<Scheme>
}

interface DealerRepository {
    fun getNearbyDealers(latitude: Double, longitude: Double): Flow<List<Dealer>>
    suspend fun searchDealers(query: String): List<Dealer>
}

interface MarketplaceRepository {
    fun getProducts(category: String? = null): Flow<List<MarketplaceProduct>>
    fun getCart(): Flow<List<CartItem>>
    suspend fun addToCart(product: MarketplaceProduct, quantity: Int = 1)
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun removeFromCart(productId: String)
    suspend fun clearCart()
    fun getOrders(): Flow<List<Order>>
    suspend fun placeOrder(deliveryAddress: String): Result<Order>
}

interface WeatherRepository {
    fun getWeather(): Flow<WeatherReport>
    suspend fun refreshWeather(latitude: Double, longitude: Double): Result<WeatherReport>
}

interface ChatRepository {
    fun getMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(prompt: String, language: String): Result<ChatMessage>
    suspend fun clearChat(): Result<Unit>
}

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    suspend fun markAsRead(id: String)
    suspend fun clearAll()
}

interface PreferencesRepository {
    fun getPreferences(): Flow<UserPreferences>
    suspend fun updateLanguage(language: String)
    suspend fun updateDarkMode(isDark: Boolean)
    suspend fun updateVoiceEnabled(enabled: Boolean)
    suspend fun updateDemoMode(isDemo: Boolean)
    suspend fun updateSupabaseConfig(url: String, anonKey: String)
    suspend fun updateGroqApiKey(apiKey: String)
}
