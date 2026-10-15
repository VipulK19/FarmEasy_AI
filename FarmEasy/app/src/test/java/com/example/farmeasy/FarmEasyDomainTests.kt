package com.example.farmeasy

import com.example.farmeasy.core.ml.SoilRecommendationEngine
import com.example.farmeasy.data.sample.SampleData
import com.example.farmeasy.domain.model.*
import com.example.farmeasy.domain.repository.AuthRepository
import com.example.farmeasy.domain.repository.FarmRepository
import com.example.farmeasy.domain.usecase.LoginUseCase
import com.example.farmeasy.domain.usecase.ManageFarmUseCase
import com.example.farmeasy.domain.usecase.RegisterUseCase
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
    fun registerUseCase_invalidPhone_returnsFailure() = runBlocking {
        val mockRepo = object : FakeAuthRepository() {}
        val registerUseCase = RegisterUseCase(mockRepo)
        val farmer = Farmer(name = "Ramesh", phone = "123")

        val result = registerUseCase(farmer, "password123")
        assertTrue(result.isFailure)
        assertEquals("A valid 10-digit mobile number is required.", result.exceptionOrNull()?.message)
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
    override suspend fun deleteCrop(cropId: String): Result<Unit> = Result.success(Unit)
}
