package com.example.farmeasy.data.repository

import com.example.farmeasy.core.ml.CropDiseaseClassifier
import com.example.farmeasy.core.ml.SoilRecommendationEngine
import com.example.farmeasy.data.local.PreferencesDataStore
import com.example.farmeasy.data.local.dao.*
import com.example.farmeasy.data.remote.groq.GroqService
import com.example.farmeasy.data.remote.supabase.SupabaseAuthService
import com.example.farmeasy.data.remote.weather.OpenMeteoService
import com.example.farmeasy.data.sample.SampleData
import com.example.farmeasy.domain.model.*
import com.example.farmeasy.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlin.math.*

class AuthRepositoryImpl(
    private val supabaseAuthService: SupabaseAuthService,
    private val preferencesDataStore: PreferencesDataStore
) : AuthRepository {
    private val currentFarmerFlow = MutableStateFlow<Farmer?>(null)

    init {
        // Initial setup with Demo Farmer
        currentFarmerFlow.value = SampleData.sampleFarmer
    }

    override fun getCurrentFarmer(): Flow<Farmer?> = currentFarmerFlow

    override suspend fun login(phoneOrEmail: String, password: String): Result<Farmer> {
        val prefs = preferencesDataStore.preferencesFlow.first()
        if (prefs.supabaseUrl.isNotBlank() && prefs.supabaseAnonKey.isNotBlank()) {
            val result = supabaseAuthService.signIn(phoneOrEmail, password, prefs.supabaseUrl, prefs.supabaseAnonKey)
            if (result.isSuccess) {
                currentFarmerFlow.value = result.getOrNull()
                return result
            }
        }
        // Fallback demo login
        val demoFarmer = SampleData.sampleFarmer.copy(
            phone = phoneOrEmail,
            email = if (phoneOrEmail.contains("@")) phoneOrEmail else "farmer@farmeasy.demo",
            isDemoUser = true
        )
        currentFarmerFlow.value = demoFarmer
        return Result.success(demoFarmer)
    }

    override suspend fun register(farmer: Farmer, password: String): Result<Farmer> {
        val prefs = preferencesDataStore.preferencesFlow.first()
        if (prefs.supabaseUrl.isNotBlank() && prefs.supabaseAnonKey.isNotBlank()) {
            val result = supabaseAuthService.signUp(farmer, password, prefs.supabaseUrl, prefs.supabaseAnonKey)
            if (result.isSuccess) {
                currentFarmerFlow.value = result.getOrNull()
                return result
            }
        }
        val demoUser = farmer.copy(isDemoUser = true)
        currentFarmerFlow.value = demoUser
        return Result.success(demoUser)
    }

    override suspend fun logout(): Result<Unit> {
        currentFarmerFlow.value = null
        return Result.success(Unit)
    }

    override suspend fun loginAsDemoFarmer(): Farmer {
        val farmer = SampleData.sampleFarmer
        currentFarmerFlow.value = farmer
        return farmer
    }

    override suspend fun updateProfile(farmer: Farmer): Result<Farmer> {
        currentFarmerFlow.value = farmer
        return Result.success(farmer)
    }
}

class FarmRepositoryImpl(
    private val farmDao: FarmDao,
    private val cropDao: CropDao
) : FarmRepository {

    suspend fun seedSampleDataIfEmpty() {
        val currentFarms = farmDao.farmsFlow.first()
        if (currentFarms.isEmpty()) {
            for (farm in SampleData.sampleFarms) {
                farmDao.insertOrUpdate(farm)
            }
            for (crop in SampleData.sampleCrops) {
                cropDao.insertOrUpdate(crop)
            }
        }
    }

    override fun getFarms(): Flow<List<Farm>> = farmDao.farmsFlow

    override suspend fun addFarm(farm: Farm): Result<Unit> {
        farmDao.insertOrUpdate(farm)
        return Result.success(Unit)
    }

    override suspend fun updateFarm(farm: Farm): Result<Unit> {
        farmDao.insertOrUpdate(farm)
        return Result.success(Unit)
    }

    override suspend fun deleteFarm(farmId: String): Result<Unit> {
        farmDao.delete(farmId)
        return Result.success(Unit)
    }

    override fun getCrops(farmId: String): Flow<List<Crop>> {
        return cropDao.getCropsForFarmFlow(farmId)
    }

    override suspend fun addCrop(crop: Crop): Result<Unit> {
        cropDao.insertOrUpdate(crop)
        return Result.success(Unit)
    }

    override suspend fun updateCrop(crop: Crop): Result<Unit> {
        cropDao.insertOrUpdate(crop)
        return Result.success(Unit)
    }

    override suspend fun deleteCrop(cropId: String, farmId: String): Result<Unit> {
        cropDao.delete(cropId, farmId)
        return Result.success(Unit)
    }
}

class DiseaseRepositoryImpl(
    private val classifier: CropDiseaseClassifier,
    private val diseaseDao: DiseaseDao
) : DiseaseRepository {

    override fun getAnalysisHistory(): Flow<List<CropDiseaseAnalysis>> = diseaseDao.historyFlow

    override suspend fun analyzeCrop(cropName: String, imageUriString: String?): Result<CropDiseaseAnalysis> {
        return try {
            val analysis = classifier.classifyImage(imageUriString, cropName)
            diseaseDao.insert(analysis)
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearHistory(): Result<Unit> {
        diseaseDao.clear()
        return Result.success(Unit)
    }
}

class SoilRepositoryImpl(
    private val engine: SoilRecommendationEngine,
    private val soilDao: SoilDao
) : SoilRepository {

    override fun getAssessmentHistory(): Flow<List<SoilAssessment>> = soilDao.historyFlow

    override suspend fun assessSoil(parameters: SoilParameters): Result<SoilAssessment> {
        return try {
            val result = engine.assess(parameters)
            soilDao.insert(result)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearHistory(): Result<Unit> {
        soilDao.clear()
        return Result.success(Unit)
    }
}

class RecommendationRepositoryImpl : RecommendationRepository {
    private val _recommendationsFlow = MutableStateFlow(SampleData.sampleRecommendations)

    override fun getRecommendations(): Flow<List<AgriculturalRecommendation>> = _recommendationsFlow

    override suspend fun refreshRecommendations(): Result<List<AgriculturalRecommendation>> {
        return Result.success(_recommendationsFlow.value)
    }
}

class SchemeRepositoryImpl : SchemeRepository {
    private val schemesFlow = MutableStateFlow(SampleData.sampleSchemes)
    private val loansFlow = MutableStateFlow(SampleData.sampleLoans)

    override fun getSchemes(): Flow<List<Scheme>> = schemesFlow
    override fun getLoans(): Flow<List<LoanInformation>> = loansFlow

    override suspend fun searchSchemes(query: String): List<Scheme> {
        if (query.isBlank()) return schemesFlow.value
        val q = query.lowercase().trim()
        return schemesFlow.value.filter {
            it.title.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.benefitSummary.lowercase().contains(q)
        }
    }
}

class DealerRepositoryImpl : DealerRepository {
    private val dealersFlow = MutableStateFlow(SampleData.sampleDealers)

    override fun getNearbyDealers(latitude: Double, longitude: Double): Flow<List<Dealer>> {
        val updated = dealersFlow.value.map { dealer ->
            val dist = calculateDistanceKm(latitude, longitude, dealer.latitude, dealer.longitude)
            dealer.copy(distanceKm = (dist * 10).roundToInt() / 10.0)
        }.sortedBy { it.distanceKm }
        dealersFlow.value = updated
        return dealersFlow
    }

    override suspend fun searchDealers(query: String): List<Dealer> {
        if (query.isBlank()) return dealersFlow.value
        val q = query.lowercase().trim()
        return dealersFlow.value.filter {
            it.businessName.lowercase().contains(q) ||
                    it.dealerType.lowercase().contains(q) ||
                    it.address.lowercase().contains(q)
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

class MarketplaceRepositoryImpl(
    private val cartDao: CartDao
) : MarketplaceRepository {
    private val productsFlow = MutableStateFlow(SampleData.sampleProducts)
    private val ordersFlow = MutableStateFlow<List<Order>>(emptyList())

    override fun getProducts(category: String?): Flow<List<MarketplaceProduct>> {
        if (category == null || category == "All") return productsFlow
        val filtered = SampleData.sampleProducts.filter { it.category == category }
        return MutableStateFlow(filtered)
    }

    override fun getCart(): Flow<List<CartItem>> = cartDao.cartFlow

    override suspend fun addToCart(product: MarketplaceProduct, quantity: Int) {
        val current = cartDao.cartFlow.first()
        val existing = current.find { it.product.id == product.id }
        if (existing != null) {
            cartDao.insertOrUpdate(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insertOrUpdate(CartItem(product = product, quantity = quantity))
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.delete(productId)
        } else {
            val current = cartDao.cartFlow.first()
            val existing = current.find { it.product.id == productId }
            if (existing != null) {
                cartDao.insertOrUpdate(existing.copy(quantity = quantity))
            }
        }
    }

    override suspend fun removeFromCart(productId: String) {
        cartDao.delete(productId)
    }

    override suspend fun clearCart() {
        cartDao.clear()
    }

    override fun getOrders(): Flow<List<Order>> = ordersFlow

    override suspend fun placeOrder(deliveryAddress: String): Result<Order> {
        val items = cartDao.cartFlow.first()
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty."))
        }
        val total = items.sumOf { it.subtotal }
        val order = Order(
            items = items,
            totalAmount = total,
            status = "Confirmed",
            deliveryAddress = deliveryAddress
        )
        ordersFlow.value = listOf(order) + ordersFlow.value
        cartDao.clear()
        return Result.success(order)
    }
}

class WeatherRepositoryImpl(
    private val openMeteoService: OpenMeteoService
) : WeatherRepository {
    private val weatherFlow = MutableStateFlow(
        WeatherReport(
            locationName = "Pune, Maharashtra",
            temperatureCelsius = 28.5,
            humidityPercent = 54,
            windSpeedKmh = 12.0,
            conditionDescription = "Partly Cloudy",
            precipitationMm = 0.0,
            advisoryNotice = "Weather conditions favorable for field operations.",
            forecast = emptyList(),
            lastUpdated = System.currentTimeMillis(),
            isCached = true
        )
    )

    override fun getWeather(): Flow<WeatherReport> = weatherFlow

    override suspend fun refreshWeather(latitude: Double, longitude: Double): Result<WeatherReport> {
        return try {
            val fresh = openMeteoService.fetchWeather(latitude, longitude)
            weatherFlow.value = fresh
            Result.success(fresh)
        } catch (e: Exception) {
            // Return cached version on failure
            val cached = weatherFlow.value.copy(isCached = true)
            weatherFlow.value = cached
            Result.success(cached)
        }
    }
}

class ChatRepositoryImpl(
    private val groqService: GroqService,
    private val chatDao: ChatDao,
    private val preferencesDataStore: PreferencesDataStore
) : ChatRepository {

    override fun getMessages(): Flow<List<ChatMessage>> = chatDao.messagesFlow

    override suspend fun sendMessage(prompt: String, language: String): Result<ChatMessage> {
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = prompt,
            language = language,
            isDemoResponse = false
        )
        chatDao.insert(userMsg)

        val prefs = preferencesDataStore.preferencesFlow.first()
        val edgeUrl = if (prefs.supabaseUrl.isNotBlank()) "${prefs.supabaseUrl}/functions/v1/groq-advisory" else null
        val response = groqService.getAgriculturalAdvice(
            prompt = prompt,
            language = language,
            apiKey = prefs.groqApiKey.ifBlank { null },
            supabaseEdgeFunctionUrl = edgeUrl
        )

        chatDao.insert(response)
        return Result.success(response)
    }

    override suspend fun clearChat(): Result<Unit> {
        chatDao.clear()
        return Result.success(Unit)
    }
}

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    suspend fun seedSampleIfEmpty() {
        val current = notificationDao.notificationsFlow.first()
        if (current.isEmpty()) {
            for (notif in SampleData.sampleNotifications) {
                notificationDao.insert(notif)
            }
        }
    }

    override fun getNotifications(): Flow<List<AppNotification>> = notificationDao.notificationsFlow

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun clearAll() {
        notificationDao.clear()
    }
}

class PreferencesRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : PreferencesRepository {
    override fun getPreferences(): Flow<UserPreferences> = preferencesDataStore.preferencesFlow
    override suspend fun updateLanguage(language: String) = preferencesDataStore.setLanguage(language)
    override suspend fun updateDarkMode(isDark: Boolean) = preferencesDataStore.setDarkMode(isDark)
    override suspend fun updateVoiceEnabled(enabled: Boolean) = preferencesDataStore.setVoiceEnabled(enabled)
    override suspend fun updateDemoMode(isDemo: Boolean) = preferencesDataStore.setDemoMode(isDemo)
    override suspend fun updateSupabaseConfig(url: String, anonKey: String) = preferencesDataStore.setSupabaseConfig(url, anonKey)
    override suspend fun updateGroqApiKey(apiKey: String) = preferencesDataStore.setGroqApiKey(apiKey)
}
