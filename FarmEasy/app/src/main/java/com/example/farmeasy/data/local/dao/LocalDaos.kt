package com.example.farmeasy.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.farmeasy.data.local.FarmEasyDatabaseHelper
import com.example.farmeasy.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class FarmDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _farmsFlow = MutableStateFlow<List<Farm>>(emptyList())
    val farmsFlow: Flow<List<Farm>> = _farmsFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<Farm>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(FarmEasyDatabaseHelper.TABLE_FARMS, null, null, null, null, null, "createdAt DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Farm(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        areaInAcres = it.getDouble(it.getColumnIndexOrThrow("areaInAcres")),
                        soilType = it.getString(it.getColumnIndexOrThrow("soilType")),
                        locationName = it.getString(it.getColumnIndexOrThrow("locationName")),
                        latitude = it.getDouble(it.getColumnIndexOrThrow("latitude")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("longitude")),
                        createdAt = it.getLong(it.getColumnIndexOrThrow("createdAt"))
                    )
                )
            }
        }
        _farmsFlow.value = list
    }

    suspend fun insertOrUpdate(farm: Farm) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", farm.id)
            put("name", farm.name)
            put("areaInAcres", farm.areaInAcres)
            put("soilType", farm.soilType)
            put("locationName", farm.locationName)
            put("latitude", farm.latitude)
            put("longitude", farm.longitude)
            put("createdAt", farm.createdAt)
        }
        db.insertWithOnConflict(
            FarmEasyDatabaseHelper.TABLE_FARMS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh()
    }

    suspend fun delete(farmId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_FARMS, "id = ?", arrayOf(farmId))
        refresh()
    }
}

class CropDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _cropsFlow = MutableStateFlow<List<Crop>>(emptyList())
    val cropsFlow: Flow<List<Crop>> = _cropsFlow

    suspend fun refresh(farmId: String) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Crop>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            FarmEasyDatabaseHelper.TABLE_CROPS,
            null,
            "farmId = ?",
            arrayOf(farmId),
            null,
            null,
            "sowingDate DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Crop(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        farmId = it.getString(it.getColumnIndexOrThrow("farmId")),
                        cropName = it.getString(it.getColumnIndexOrThrow("cropName")),
                        variety = it.getString(it.getColumnIndexOrThrow("variety")),
                        sowingDate = it.getLong(it.getColumnIndexOrThrow("sowingDate")),
                        expectedHarvestDate = it.getLong(it.getColumnIndexOrThrow("expectedHarvestDate")),
                        growthStage = it.getString(it.getColumnIndexOrThrow("growthStage")),
                        notes = it.getString(it.getColumnIndexOrThrow("notes")) ?: ""
                    )
                )
            }
        }
        _cropsFlow.value = list
    }

    suspend fun insertOrUpdate(crop: Crop) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", crop.id)
            put("farmId", crop.farmId)
            put("cropName", crop.cropName)
            put("variety", crop.variety)
            put("sowingDate", crop.sowingDate)
            put("expectedHarvestDate", crop.expectedHarvestDate)
            put("growthStage", crop.growthStage)
            put("notes", crop.notes)
        }
        db.insertWithOnConflict(
            FarmEasyDatabaseHelper.TABLE_CROPS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh(crop.farmId)
    }

    suspend fun delete(cropId: String, farmId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_CROPS, "id = ?", arrayOf(cropId))
        refresh(farmId)
    }
}

class DiseaseDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _historyFlow = MutableStateFlow<List<CropDiseaseAnalysis>>(emptyList())
    val historyFlow: Flow<List<CropDiseaseAnalysis>> = _historyFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<CropDiseaseAnalysis>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FarmEasyDatabaseHelper.TABLE_DISEASE_ANALYSIS,
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val prediction = DiseasePrediction(
                    conditionName = it.getString(it.getColumnIndexOrThrow("conditionName")),
                    scientificName = it.getString(it.getColumnIndexOrThrow("scientificName")) ?: "",
                    confidence = it.getFloat(it.getColumnIndexOrThrow("confidence")),
                    severity = it.getString(it.getColumnIndexOrThrow("severity")),
                    symptoms = it.getString(it.getColumnIndexOrThrow("symptoms")) ?: "",
                    biologicalTreatment = it.getString(it.getColumnIndexOrThrow("biologicalTreatment")) ?: "",
                    chemicalTreatment = it.getString(it.getColumnIndexOrThrow("chemicalTreatment")) ?: "",
                    preventiveMeasures = it.getString(it.getColumnIndexOrThrow("preventiveMeasures")) ?: ""
                )
                list.add(
                    CropDiseaseAnalysis(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        cropName = it.getString(it.getColumnIndexOrThrow("cropName")),
                        imageUri = it.getString(it.getColumnIndexOrThrow("imageUri")),
                        topPrediction = prediction,
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        isModelAvailable = it.getInt(it.getColumnIndexOrThrow("isModelAvailable")) == 1,
                        isDemoResult = it.getInt(it.getColumnIndexOrThrow("isDemoResult")) == 1
                    )
                )
            }
        }
        _historyFlow.value = list
    }

    suspend fun insert(analysis: CropDiseaseAnalysis) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", analysis.id)
            put("cropName", analysis.cropName)
            put("imageUri", analysis.imageUri)
            put("conditionName", analysis.topPrediction.conditionName)
            put("scientificName", analysis.topPrediction.scientificName)
            put("confidence", analysis.topPrediction.confidence)
            put("severity", analysis.topPrediction.severity)
            put("symptoms", analysis.topPrediction.symptoms)
            put("biologicalTreatment", analysis.topPrediction.biologicalTreatment)
            put("chemicalTreatment", analysis.topPrediction.chemicalTreatment)
            put("preventiveMeasures", analysis.topPrediction.preventiveMeasures)
            put("timestamp", analysis.timestamp)
            put("isModelAvailable", if (analysis.isModelAvailable) 1 else 0)
            put("isDemoResult", if (analysis.isDemoResult) 1 else 0)
        }
        db.insertWithOnConflict(
            FarmEasyDatabaseHelper.TABLE_DISEASE_ANALYSIS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_DISEASE_ANALYSIS, null, null)
        refresh()
    }
}

class SoilDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _historyFlow = MutableStateFlow<List<SoilAssessment>>(emptyList())
    val historyFlow: Flow<List<SoilAssessment>> = _historyFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<SoilAssessment>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            FarmEasyDatabaseHelper.TABLE_SOIL_ASSESSMENT,
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                val params = SoilParameters(
                    ph = it.getDouble(it.getColumnIndexOrThrow("ph")),
                    nitrogen = it.getDouble(it.getColumnIndexOrThrow("nitrogen")),
                    phosphorus = it.getDouble(it.getColumnIndexOrThrow("phosphorus")),
                    potassium = it.getDouble(it.getColumnIndexOrThrow("potassium")),
                    moisturePercent = it.getDouble(it.getColumnIndexOrThrow("moisturePercent")),
                    soilType = it.getString(it.getColumnIndexOrThrow("soilType")),
                    intendedCrop = it.getString(it.getColumnIndexOrThrow("intendedCrop"))
                )
                val rawCrops = it.getString(it.getColumnIndexOrThrow("recommendedCrops")) ?: ""
                list.add(
                    SoilAssessment(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        parameters = params,
                        fertilityScore = it.getInt(it.getColumnIndexOrThrow("fertilityScore")),
                        fertilityClass = it.getString(it.getColumnIndexOrThrow("fertilityClass")),
                        organicCarbonStatus = it.getString(it.getColumnIndexOrThrow("organicCarbonStatus")),
                        fertilizerRecommendation = it.getString(it.getColumnIndexOrThrow("fertilizerRecommendation")),
                        recommendedCrops = rawCrops.split(",").map { c -> c.trim() }.filter { c -> c.isNotBlank() },
                        safetyNotice = it.getString(it.getColumnIndexOrThrow("safetyNotice")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        isDemoResult = it.getInt(it.getColumnIndexOrThrow("isDemoResult")) == 1
                    )
                )
            }
        }
        _historyFlow.value = list
    }

    suspend fun insert(assessment: SoilAssessment) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", assessment.id)
            put("ph", assessment.parameters.ph)
            put("nitrogen", assessment.parameters.nitrogen)
            put("phosphorus", assessment.parameters.phosphorus)
            put("potassium", assessment.parameters.potassium)
            put("moisturePercent", assessment.parameters.moisturePercent)
            put("soilType", assessment.parameters.soilType)
            put("intendedCrop", assessment.parameters.intendedCrop)
            put("fertilityScore", assessment.fertilityScore)
            put("fertilityClass", assessment.fertilityClass)
            put("organicCarbonStatus", assessment.organicCarbonStatus)
            put("fertilizerRecommendation", assessment.fertilizerRecommendation)
            put("recommendedCrops", assessment.recommendedCrops.joinToString(","))
            put("safetyNotice", assessment.safetyNotice)
            put("timestamp", assessment.timestamp)
            put("isDemoResult", if (assessment.isDemoResult) 1 else 0)
        }
        db.insertWithOnConflict(
            FarmEasyDatabaseHelper.TABLE_SOIL_ASSESSMENT,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_SOIL_ASSESSMENT, null, null)
        refresh()
    }
}

class CartDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _cartFlow = MutableStateFlow<List<CartItem>>(emptyList())
    val cartFlow: Flow<List<CartItem>> = _cartFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<CartItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(FarmEasyDatabaseHelper.TABLE_CART, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val product = MarketplaceProduct(
                    id = it.getString(it.getColumnIndexOrThrow("productId")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    category = it.getString(it.getColumnIndexOrThrow("category")),
                    brand = it.getString(it.getColumnIndexOrThrow("brand")),
                    price = it.getDouble(it.getColumnIndexOrThrow("price")),
                    unit = it.getString(it.getColumnIndexOrThrow("unit")),
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
                val qty = it.getInt(it.getColumnIndexOrThrow("quantity"))
                list.add(CartItem(product = product, quantity = qty))
            }
        }
        _cartFlow.value = list
    }

    suspend fun insertOrUpdate(item: CartItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("productId", item.product.id)
            put("name", item.product.name)
            put("category", item.product.category)
            put("brand", item.product.brand)
            put("price", item.product.price)
            put("unit", item.product.unit)
            put("description", item.product.description)
            put("quantity", item.quantity)
        }
        db.insertWithOnConflict(
            FarmEasyDatabaseHelper.TABLE_CART,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        refresh()
    }

    suspend fun delete(productId: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_CART, "productId = ?", arrayOf(productId))
        refresh()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_CART, null, null)
        refresh()
    }
}

class ChatDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messagesFlow: Flow<List<ChatMessage>> = _messagesFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<ChatMessage>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(FarmEasyDatabaseHelper.TABLE_CHAT, null, null, null, null, null, "timestamp ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ChatMessage(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        sender = if (it.getString(it.getColumnIndexOrThrow("sender")) == "USER") MessageSender.USER else MessageSender.ASSISTANT,
                        text = it.getString(it.getColumnIndexOrThrow("text")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        language = it.getString(it.getColumnIndexOrThrow("language")),
                        isDemoResponse = it.getInt(it.getColumnIndexOrThrow("isDemoResponse")) == 1
                    )
                )
            }
        }
        _messagesFlow.value = list
    }

    suspend fun insert(message: ChatMessage) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", message.id)
            put("sender", message.sender.name)
            put("text", message.text)
            put("timestamp", message.timestamp)
            put("language", message.language)
            put("isDemoResponse", if (message.isDemoResponse) 1 else 0)
        }
        db.insertWithOnConflict(FarmEasyDatabaseHelper.TABLE_CHAT, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        refresh()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_CHAT, null, null)
        refresh()
    }
}

class NotificationDao(private val dbHelper: FarmEasyDatabaseHelper) {
    private val _notificationsFlow = MutableStateFlow<List<AppNotification>>(emptyList())
    val notificationsFlow: Flow<List<AppNotification>> = _notificationsFlow

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val list = mutableListOf<AppNotification>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(FarmEasyDatabaseHelper.TABLE_NOTIFICATIONS, null, null, null, null, null, "timestamp DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    AppNotification(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        message = it.getString(it.getColumnIndexOrThrow("message")),
                        category = it.getString(it.getColumnIndexOrThrow("category")),
                        timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp")),
                        isRead = it.getInt(it.getColumnIndexOrThrow("isRead")) == 1
                    )
                )
            }
        }
        _notificationsFlow.value = list
    }

    suspend fun insert(item: AppNotification) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id", item.id)
            put("title", item.title)
            put("message", item.message)
            put("category", item.category)
            put("timestamp", item.timestamp)
            put("isRead", if (item.isRead) 1 else 0)
        }
        db.insertWithOnConflict(FarmEasyDatabaseHelper.TABLE_NOTIFICATIONS, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        refresh()
    }

    suspend fun markAsRead(id: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("isRead", 1) }
        db.update(FarmEasyDatabaseHelper.TABLE_NOTIFICATIONS, values, "id = ?", arrayOf(id))
        refresh()
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete(FarmEasyDatabaseHelper.TABLE_NOTIFICATIONS, null, null)
        refresh()
    }
}
