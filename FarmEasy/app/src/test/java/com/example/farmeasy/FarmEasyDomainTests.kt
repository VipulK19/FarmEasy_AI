package com.example.farmeasy

import com.example.farmeasy.core.ml.SoilRecommendationEngine
import com.example.farmeasy.data.sample.SampleData
import com.example.farmeasy.domain.model.*
import com.example.farmeasy.domain.repository.AuthRepository
import com.example.farmeasy.domain.repository.FarmRepository
import com.example.farmeasy.domain.repository.MarketplaceRepository
import com.example.farmeasy.domain.usecase.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FarmEasyDomainTests {

    private val soilEngine = SoilRecommendationEngine()

    @Test
    fun soilParameters_validRanges_returnsTrue() {
        val validParams = SoilParameters(
            ph = 6.8,
            nitrogen = 180.0,
            phosphorus = 35.0,
            potassium = 210.0,
            moisturePercent = 25.0
        )
        assertTrue(validParams.isValid())
    }

    @Test
    fun soilParameters_invalidPh_returnsFalse() {
        val invalidPh = SoilParameters(
            ph = 12.5,
            nitrogen = 180.0,
            phosphorus = 35.0,
            potassium = 210.0
        )
        assertFalse(invalidPh.isValid())
    }

    @Test
    fun soilParameters_negativeNitrogen_returnsFalse() {
        val negativeN = SoilParameters(
            ph = 6.5,
            nitrogen = -10.0,
            phosphorus = 35.0,
            potassium = 210.0
        )
        assertFalse(negativeN.isValid())
    }

    @Test
    fun soilParameters_invalidMoisture_returnsFalse() {
        val invalidMoisture = SoilParameters(
            ph = 6.5,
            nitrogen = 180.0,
            phosphorus = 35.0,
            potassium = 210.0,
            moisturePercent = 105.0
        )
        assertFalse(invalidMoisture.isValid())
    }

    @Test
    fun soilEngine_optimalParameters_returnsHighFertility() {
        val params = SoilParameters(
            ph = 6.8,
            nitrogen = 220.0,
            phosphorus = 45.0,
            potassium = 250.0,
            intendedCrop = "Wheat"
        )
        val assessment = soilEngine.assess(params)
        assertTrue("Expected score >= 80 but got ${assessment.fertilityScore}", assessment.fertilityScore >= 80)
        assertTrue(assessment.fertilityClass.contains("High"))
        assertTrue(assessment.recommendedCrops.contains("Wheat"))
    }

    @Test
    fun soilEngine_acidicSoil_recommendsAgriculturalLime() {
        val params = SoilParameters(
            ph = 5.2,
            nitrogen = 110.0,
            phosphorus = 15.0,
            potassium = 90.0,
            intendedCrop = "Potato"
        )
        val assessment = soilEngine.assess(params)
        assertTrue(assessment.fertilityScore < 60)
        assertTrue(assessment.fertilizerRecommendation.contains("lime") || assessment.fertilizerRecommendation.contains("dolomite"))
    }

    @Test
    fun soilEngine_alkalineSoil_recommendsGypsum() {
        val params = SoilParameters(
            ph = 8.5,
            nitrogen = 180.0,
            phosphorus = 30.0,
            potassium = 200.0,
            intendedCrop = "Cotton"
        )
        val assessment = soilEngine.assess(params)
        assertTrue(assessment.fertilizerRecommendation.contains("gypsum"))
    }

    @Test
    fun cartItem_subtotalCalculatesCorrectly() {
        val product = MarketplaceProduct(
            id = "test_p1",
            name = "Urea Bag",
            category = "Fertilizers",
            brand = "IFFCO",
            price = 266.50,
            unit = "45kg",
            description = "Test description"
        )
        val cartItem = CartItem(product = product, quantity = 3)
        assertEquals(799.50, cartItem.subtotal, 0.001)
    }

    @Test
    fun loginUseCase_emptyIdentifier_returnsFailure() = runBlocking {
        val mockRepo = object : FakeAuthRepository() {}
        val loginUseCase = LoginUseCase(mockRepo)

        val result = loginUseCase("   ", "password123")
        assertTrue(result.isFailure)
        assertEquals("Phone number or email is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun loginUseCase_shortPassword_returnsFailure() = runBlocking {
        val mockRepo = object : FakeAuthRepository() {}
        val loginUseCase = LoginUseCase(mockRepo)

        val result = loginUseCase("9876543210", "123")
        assertTrue(result.isFailure)
        assertEquals("Password must be at least 6 characters long.", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUseCase_emptyName_returnsFailure() = runBlocking {
        val mockRepo = object : FakeAuthRepository() {}
        val registerUseCase = RegisterUseCase(mockRepo)
        val farmer = Farmer(name = "   ", phone = "9876543210")

        val result = registerUseCase(farmer, "password123")
        assertTrue(result.isFailure)
        assertEquals("Full name is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun registerUseCase_invalidPhone_returnsFailure() = runBlocking {
        val mockRepo = object : FakeAuthRepository() {}
        val registerUseCase = RegisterUseCase(mockRepo)
        val farmer = Farmer(name = "Ramesh", phone = "123")

        val result = registerUseCase(farmer, "password123")
        assertTrue(result.isFailure)
        assertEquals("A valid 10-digit mobile number is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun manageFarmUseCase_blankFarmName_returnsFailure() = runBlocking {
        val mockRepo = object : FakeFarmRepository() {}
        val manageFarm = ManageFarmUseCase(mockRepo)
        val farm = Farm(name = "   ", areaInAcres = 2.5)

        val result = manageFarm.saveFarm(farm)
        assertTrue(result.isFailure)
        assertEquals("Farm name is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun manageFarmUseCase_zeroAcreage_returnsFailure() = runBlocking {
        val mockRepo = object : FakeFarmRepository() {}
        val manageFarm = ManageFarmUseCase(mockRepo)
        val farm = Farm(name = "Plot 1", areaInAcres = 0.0)

        val result = manageFarm.saveFarm(farm)
        assertTrue(result.isFailure)
        assertEquals("Farm area must be greater than 0 acres.", result.exceptionOrNull()?.message)
    }

    @Test
    fun manageFarmUseCase_blankCropName_returnsFailure() = runBlocking {
        val mockRepo = object : FakeFarmRepository() {}
        val manageFarm = ManageFarmUseCase(mockRepo)
        val crop = Crop(farmId = "farm_1", cropName = "  ")

        val result = manageFarm.saveCrop(crop)
        assertTrue(result.isFailure)
        assertEquals("Crop name is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun manageMarketplaceUseCase_checkoutBlankAddress_returnsFailure() = runBlocking {
        val mockRepo = object : FakeMarketplaceRepository() {}
        val manageMarketplace = ManageMarketplaceUseCase(mockRepo)

        val result = manageMarketplace.checkout("   ")
        assertTrue(result.isFailure)
        assertEquals("Delivery address is required for ordering.", result.exceptionOrNull()?.message)
    }

    @Test
    fun analyzeDiseaseUseCase_blankCropName_returnsFailure() = runBlocking {
        val mockRepo = object : FakeDiseaseRepository() {}
        val analyzeDisease = AnalyzeDiseaseUseCase(mockRepo)

        val result = analyzeDisease.analyze("  ", null)
        assertTrue(result.isFailure)
        assertEquals("Please specify crop name.", result.exceptionOrNull()?.message)
    }

    @Test
    fun sendChatMessageUseCase_blankPrompt_returnsFailure() = runBlocking {
        val mockRepo = object : FakeChatRepository() {}
        val sendChatMessage = SendChatMessageUseCase(mockRepo)

        val result = sendChatMessage.send("    ", "English")
        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty.", result.exceptionOrNull()?.message)
    }

    @Test
    fun groqService_liveApiKey_returnsDynamicAdvice() = runBlocking {
        try {
            val client = com.example.farmeasy.data.remote.KtorClientProvider.client
            val groqService = com.example.farmeasy.data.remote.groq.GroqService(client)
            val apiKey = com.example.farmeasy.BuildConfig.GROQ_API_KEY
            val response = groqService.getAgriculturalAdvice(
                prompt = "What is the best fertilizer dose for wheat at tillering stage in Pune?",
                language = "English",
                apiKey = apiKey,
                supabaseEdgeFunctionUrl = null
            )
            println("GROQ_TEST_RESULT: text='${response.text}', isDemo=${response.isDemoResponse}")
            assertNotNull(response)
            assertTrue(response.text.isNotBlank())
        } catch (e: Exception) {
            println("GROQ_TEST_EXCEPTION: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
        }
    }

    @Test
    fun sampleData_containsExpectedSchemes() {
        assertFalse(SampleData.sampleSchemes.isEmpty())
        val pmKisan = SampleData.sampleSchemes.find { it.id == "scheme_pm_kisan" }
        assertNotNull(pmKisan)
        assertEquals("Pradhan Mantri Kisan Samman Nidhi (PM-KISAN)", pmKisan?.title)
    }
}

open class FakeAuthRepository : AuthRepository {
    override fun getCurrentFarmer(): Flow<Farmer?> = flowOf(null)
    override suspend fun login(phoneOrEmail: String, password: String): Result<Farmer> = Result.success(SampleData.sampleFarmer)
    override suspend fun register(farmer: Farmer, password: String): Result<Farmer> = Result.success(farmer)
    override suspend fun logout(): Result<Unit> = Result.success(Unit)
    override suspend fun loginAsDemoFarmer(): Farmer = SampleData.sampleFarmer
    override suspend fun updateProfile(farmer: Farmer): Result<Farmer> = Result.success(farmer)
}

open class FakeFarmRepository : FarmRepository {
    override fun getFarms(): Flow<List<Farm>> = flowOf(emptyList())
    override suspend fun addFarm(farm: Farm): Result<Unit> = Result.success(Unit)
    override suspend fun updateFarm(farm: Farm): Result<Unit> = Result.success(Unit)
    override suspend fun deleteFarm(farmId: String): Result<Unit> = Result.success(Unit)
    override fun getCrops(farmId: String): Flow<List<Crop>> = flowOf(emptyList())
    override suspend fun addCrop(crop: Crop): Result<Unit> = Result.success(Unit)
    override suspend fun updateCrop(crop: Crop): Result<Unit> = Result.success(Unit)
    override suspend fun deleteCrop(cropId: String, farmId: String): Result<Unit> = Result.success(Unit)
}

open class FakeMarketplaceRepository : MarketplaceRepository {
    override fun getProducts(category: String?): Flow<List<MarketplaceProduct>> = flowOf(emptyList())
    override fun getCart(): Flow<List<CartItem>> = flowOf(emptyList())
    override suspend fun addToCart(product: MarketplaceProduct, quantity: Int) {}
    override suspend fun updateQuantity(productId: String, quantity: Int) {}
    override suspend fun removeFromCart(productId: String) {}
    override suspend fun clearCart() {}
    override fun getOrders(): Flow<List<Order>> = flowOf(emptyList())
    override suspend fun placeOrder(deliveryAddress: String): Result<Order> = Result.success(
        Order(items = emptyList(), totalAmount = 0.0, deliveryAddress = deliveryAddress)
    )
}

open class FakeDiseaseRepository : com.example.farmeasy.domain.repository.DiseaseRepository {
    override fun getAnalysisHistory(): Flow<List<CropDiseaseAnalysis>> = flowOf(emptyList())
    override suspend fun analyzeCrop(cropName: String, imageUriString: String?): Result<CropDiseaseAnalysis> {
        val top = DiseasePrediction("Test Blight", "Test", 0.9f, "Low", "", "", "", "")
        return Result.success(CropDiseaseAnalysis(cropName = cropName, topPrediction = top))
    }
    override suspend fun clearHistory(): Result<Unit> = Result.success(Unit)
}

open class FakeChatRepository : com.example.farmeasy.domain.repository.ChatRepository {
    override fun getMessages(): Flow<List<ChatMessage>> = flowOf(emptyList())
    override suspend fun sendMessage(prompt: String, language: String): Result<ChatMessage> {
        return Result.success(ChatMessage(sender = MessageSender.ASSISTANT, text = "Reply", language = language))
    }
    override suspend fun clearChat(): Result<Unit> = Result.success(Unit)
}
