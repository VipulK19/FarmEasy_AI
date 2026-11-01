package com.example.farmeasy.domain.model

import com.example.farmeasy.BuildConfig
import java.util.UUID

/**
 * Farmer profile domain model
 */
data class Farmer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String = "",
    val state: String = "Maharashtra",
    val district: String = "Pune",
    val preferredLanguage: String = "English", // English, Hindi, Marathi
    val preferredCrops: List<String> = emptyList(),
    val isDemoUser: Boolean = false
)

/**
 * Farm domain model
 */
data class Farm(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val areaInAcres: Double,
    val soilType: String = "Black Soil", // Black, Red, Alluvial, Clay, Sandy
    val locationName: String = "Farm Parcel 1",
    val latitude: Double = 18.5204,
    val longitude: Double = 73.8567,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Crop within a farm
 */
data class Crop(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val cropName: String,
    val variety: String = "",
    val sowingDate: Long = System.currentTimeMillis(),
    val expectedHarvestDate: Long = System.currentTimeMillis() + (90L * 24 * 3600 * 1000),
    val growthStage: String = "Vegetative", // Sowing, Germination, Vegetative, Flowering, Maturity, Harvest
    val notes: String = ""
)

/**
 * Single disease prediction with confidence
 */
data class DiseasePrediction(
    val conditionName: String,
    val scientificName: String = "",
    val confidence: Float, // 0.0 to 1.0
    val severity: String = "Moderate", // Low, Moderate, Severe
    val symptoms: String = "",
    val biologicalTreatment: String = "",
    val chemicalTreatment: String = "",
    val preventiveMeasures: String = ""
)

/**
 * Crop disease analysis record
 */
data class CropDiseaseAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val cropName: String,
    val imageUri: String? = null,
    val topPrediction: DiseasePrediction,
    val alternativePredictions: List<DiseasePrediction> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isModelAvailable: Boolean = true,
    val isDemoResult: Boolean = false
)

/**
 * Input parameters for soil assessment
 */
data class SoilParameters(
    val ph: Double, // typical 4.5 to 9.0
    val nitrogen: Double, // kg/ha (e.g. 50 - 300)
    val phosphorus: Double, // kg/ha (e.g. 10 - 80)
    val potassium: Double, // kg/ha (e.g. 50 - 400)
    val moisturePercent: Double = 25.0,
    val soilType: String = "Loamy",
    val intendedCrop: String = "Wheat"
) {
    fun isValid(): Boolean {
        return ph in 3.0..10.0 &&
                nitrogen >= 0.0 &&
                phosphorus >= 0.0 &&
                potassium >= 0.0 &&
                moisturePercent in 0.0..100.0
    }
}

/**
 * Soil fertility assessment result
 */
data class SoilAssessment(
    val id: String = UUID.randomUUID().toString(),
    val parameters: SoilParameters,
    val fertilityScore: Int, // 0 to 100
    val fertilityClass: String, // High, Medium, Low, Acidic, Alkaline
    val organicCarbonStatus: String = "Medium",
    val fertilizerRecommendation: String,
    val recommendedCrops: List<String>,
    val safetyNotice: String = "Values reflect user-supplied parameters. Confirm with a lab soil health card before major fertilizer investment.",
    val timestamp: Long = System.currentTimeMillis(),
    val isDemoResult: Boolean = false
)

/**
 * Agricultural recommendation
 */
data class AgriculturalRecommendation(
    val id: String = UUID.randomUUID().toString(),
    val category: String, // Disease, Soil, Irrigation, Weather, Fertilizer
    val title: String,
    val actionSuggested: String,
    val rationale: String,
    val safetyPrecautions: String,
    val confidenceScore: Float = 0.88f,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Government scheme or subsidy
 */
data class Scheme(
    val id: String,
    val title: String,
    val ministry: String,
    val benefitSummary: String,
    val eligibility: String,
    val requiredDocuments: List<String>,
    val applicationSteps: String,
    val officialUrl: String,
    val lastVerifiedDate: String = "2026-03-01",
    val category: String = "Financial Support" // Subsidy, Insurance, Irrigation, Machinery
)

/**
 * Agricultural loan / credit information
 */
data class LoanInformation(
    val id: String,
    val bankName: String,
    val schemeName: String,
    val interestRate: String,
    val maximumAmount: String,
    val tenure: String,
    val eligibilityCriteria: String,
    val officialUrl: String
)

/**
 * Agricultural dealer / supplier locator
 */
data class Dealer(
    val id: String,
    val businessName: String,
    val dealerType: String, // Fertilizers, Seeds, Pesticides, Equipment, Multi-brand
    val address: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double = 0.0,
    val verified: Boolean = true
)

/**
 * Marketplace product
 */
data class MarketplaceProduct(
    val id: String,
    val name: String,
    val category: String, // Seeds, Fertilizers, Bio-Pesticides, Tools, Irrigation
    val brand: String,
    val price: Double,
    val unit: String, // 50kg bag, 1L bottle, Pack
    val description: String,
    val inStock: Boolean = true,
    val rating: Float = 4.6f
)

/**
 * Cart item
 */
data class CartItem(
    val product: MarketplaceProduct,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity
}

/**
 * Order details
 */
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: String = "Confirmed",
    val deliveryAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Weather forecast items
 */
data class ForecastItem(
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCondition: String,
    val precipitationMm: Double
)

/**
 * Current & forecast weather report
 */
data class WeatherReport(
    val locationName: String = "Pune, Maharashtra",
    val temperatureCelsius: Double = 28.5,
    val humidityPercent: Int = 54,
    val windSpeedKmh: Double = 12.0,
    val conditionDescription: String = "Partly Cloudy",
    val precipitationMm: Double = 0.0,
    val advisoryNotice: String = "Favorable weather for field spraying and vegetative growth.",
    val forecast: List<ForecastItem> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val isCached: Boolean = false
)

/**
 * Chat message for AI Assistant
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "English",
    val isDemoResponse: Boolean = false
)

enum class MessageSender {
    USER,
    ASSISTANT
}

/**
 * Notification / advisory alert
 */
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val category: String, // Weather, Advisory, Scheme, Order
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * User Preferences
 */
data class UserPreferences(
    val isDarkMode: Boolean = false,
    val selectedLanguage: String = "English",
    val isVoiceEnabled: Boolean = true,
    val isDemoModeActive: Boolean = true,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val groqApiKey: String = BuildConfig.GROQ_API_KEY
)
