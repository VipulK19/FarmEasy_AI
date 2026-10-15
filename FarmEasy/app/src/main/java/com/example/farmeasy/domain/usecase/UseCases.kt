package com.example.farmeasy.domain.usecase

import com.example.farmeasy.domain.model.*
import com.example.farmeasy.domain.repository.*
import kotlinx.coroutines.flow.Flow

/**
 * Authentication and Farmer Profile Use Cases
 */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(identifier: String, password: String): Result<Farmer> {
        val trimmedIdentifier = identifier.trim()
        if (trimmedIdentifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number or email is required."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
        }
        return repository.login(trimmedIdentifier, password)
    }
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(farmer: Farmer, password: String): Result<Farmer> {
        if (farmer.name.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Full name is required."))
        }
        if (farmer.phone.trim().length < 10) {
            return Result.failure(IllegalArgumentException("A valid 10-digit mobile number is required."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        return repository.register(farmer, password)
    }
}

class GetFarmerProfileUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Farmer?> = repository.getCurrentFarmer()
}

class LoginDemoUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Farmer = repository.loginAsDemoFarmer()
}

/**
 * Farm and Crop Management Use Cases
 */
class ManageFarmUseCase(private val repository: FarmRepository) {
    fun getFarms(): Flow<List<Farm>> = repository.getFarms()

    suspend fun saveFarm(farm: Farm): Result<Unit> {
        if (farm.name.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Farm name is required."))
        }
        if (farm.areaInAcres <= 0.0) {
            return Result.failure(IllegalArgumentException("Farm area must be greater than 0 acres."))
        }
        return repository.addFarm(farm)
    }

    suspend fun deleteFarm(farmId: String): Result<Unit> {
        return repository.deleteFarm(farmId)
    }

    fun getCrops(farmId: String): Flow<List<Crop>> = repository.getCrops(farmId)

    suspend fun saveCrop(crop: Crop): Result<Unit> {
        if (crop.cropName.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Crop name is required."))
        }
        return repository.addCrop(crop)
    }

    suspend fun deleteCrop(cropId: String): Result<Unit> {
        return repository.deleteCrop(cropId)
    }
}

/**
 * Crop Disease Detection Use Case
 */
class AnalyzeDiseaseUseCase(private val repository: DiseaseRepository) {
    fun getHistory(): Flow<List<CropDiseaseAnalysis>> = repository.getAnalysisHistory()

    suspend fun analyze(cropName: String, imageUri: String?): Result<CropDiseaseAnalysis> {
        if (cropName.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Please specify crop name."))
        }
        return repository.analyzeCrop(cropName.trim(), imageUri)
    }

    suspend fun clearHistory(): Result<Unit> = repository.clearHistory()
}

/**
 * Soil Fertility Assessment Use Case
 */
class AssessSoilUseCase(private val repository: SoilRepository) {
    fun getHistory(): Flow<List<SoilAssessment>> = repository.getAssessmentHistory()

    suspend fun assess(parameters: SoilParameters): Result<SoilAssessment> {
        if (!parameters.isValid()) {
            return Result.failure(IllegalArgumentException("Invalid parameters: Ensure pH (3.0–10.0), positive NPK values, and valid moisture (0–100%)."))
        }
        return repository.assessSoil(parameters)
    }

    suspend fun clearHistory(): Result<Unit> = repository.clearHistory()
}

/**
 * Weather & Advisory Use Cases
 */
class GetWeatherUseCase(private val repository: WeatherRepository) {
    operator fun invoke(): Flow<WeatherReport> = repository.getWeather()
    suspend fun refresh(lat: Double, lng: Double): Result<WeatherReport> = repository.refreshWeather(lat, lng)
}

/**
 * AI Chatbot Use Case
 */
class SendChatMessageUseCase(private val repository: ChatRepository) {
    fun getMessages(): Flow<List<ChatMessage>> = repository.getMessages()

    suspend fun send(prompt: String, language: String): Result<ChatMessage> {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }
        return repository.sendMessage(trimmed, language)
    }

    suspend fun clear(): Result<Unit> = repository.clearChat()
}

/**
 * Marketplace & Cart Use Case
 */
class ManageMarketplaceUseCase(private val repository: MarketplaceRepository) {
    fun getProducts(category: String? = null): Flow<List<MarketplaceProduct>> = repository.getProducts(category)
    fun getCart(): Flow<List<CartItem>> = repository.getCart()
    fun getOrders(): Flow<List<Order>> = repository.getOrders()

    suspend fun addToCart(product: MarketplaceProduct, quantity: Int = 1) = repository.addToCart(product, quantity)
    suspend fun updateQuantity(productId: String, quantity: Int) = repository.updateQuantity(productId, quantity)
    suspend fun removeFromCart(productId: String) = repository.removeFromCart(productId)

    suspend fun checkout(address: String): Result<Order> {
        if (address.trim().isBlank()) {
            return Result.failure(IllegalArgumentException("Delivery address is required for ordering."))
        }
        return repository.placeOrder(address.trim())
    }
}
