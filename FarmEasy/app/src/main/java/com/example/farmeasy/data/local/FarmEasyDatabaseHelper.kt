package com.example.farmeasy.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FarmEasyDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "farmeasy_local.db"
        const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_FARMS = "farms"
        const val TABLE_CROPS = "crops"
        const val TABLE_DISEASE_ANALYSIS = "disease_analysis"
        const val TABLE_SOIL_ASSESSMENT = "soil_assessment"
        const val TABLE_RECOMMENDATIONS = "recommendations"
        const val TABLE_CART = "cart_items"
        const val TABLE_ORDERS = "orders"
        const val TABLE_WEATHER = "weather_cache"
        const val TABLE_CHAT = "chat_messages"
        const val TABLE_NOTIFICATIONS = "notifications"
        const val TABLE_PREFERENCES = "user_preferences"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Farms
        db.execSQL("""
            CREATE TABLE $TABLE_FARMS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                areaInAcres REAL NOT NULL,
                soilType TEXT NOT NULL,
                locationName TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                createdAt INTEGER NOT NULL
            );
        """.trimIndent())

        // Crops
        db.execSQL("""
            CREATE TABLE $TABLE_CROPS (
                id TEXT PRIMARY KEY,
                farmId TEXT NOT NULL,
                cropName TEXT NOT NULL,
                variety TEXT NOT NULL,
                sowingDate INTEGER NOT NULL,
                expectedHarvestDate INTEGER NOT NULL,
                growthStage TEXT NOT NULL,
                notes TEXT,
                FOREIGN KEY (farmId) REFERENCES $TABLE_FARMS(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // Crop Disease Analyses
        db.execSQL("""
            CREATE TABLE $TABLE_DISEASE_ANALYSIS (
                id TEXT PRIMARY KEY,
                cropName TEXT NOT NULL,
                imageUri TEXT,
                conditionName TEXT NOT NULL,
                scientificName TEXT,
                confidence REAL NOT NULL,
                severity TEXT NOT NULL,
                symptoms TEXT,
                biologicalTreatment TEXT,
                chemicalTreatment TEXT,
                preventiveMeasures TEXT,
                timestamp INTEGER NOT NULL,
                isModelAvailable INTEGER NOT NULL,
                isDemoResult INTEGER NOT NULL
            );
        """.trimIndent())

        // Soil Assessments
        db.execSQL("""
            CREATE TABLE $TABLE_SOIL_ASSESSMENT (
                id TEXT PRIMARY KEY,
                ph REAL NOT NULL,
                nitrogen REAL NOT NULL,
                phosphorus REAL NOT NULL,
                potassium REAL NOT NULL,
                moisturePercent REAL NOT NULL,
                soilType TEXT NOT NULL,
                intendedCrop TEXT NOT NULL,
                fertilityScore INTEGER NOT NULL,
                fertilityClass TEXT NOT NULL,
                organicCarbonStatus TEXT NOT NULL,
                fertilizerRecommendation TEXT NOT NULL,
                recommendedCrops TEXT NOT NULL,
                safetyNotice TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isDemoResult INTEGER NOT NULL
            );
        """.trimIndent())

        // Recommendations
        db.execSQL("""
            CREATE TABLE $TABLE_RECOMMENDATIONS (
                id TEXT PRIMARY KEY,
                category TEXT NOT NULL,
                title TEXT NOT NULL,
                actionSuggested TEXT NOT NULL,
                rationale TEXT NOT NULL,
                safetyPrecautions TEXT NOT NULL,
                confidenceScore REAL NOT NULL,
                createdAt INTEGER NOT NULL
            );
        """.trimIndent())

        // Cart items
        db.execSQL("""
            CREATE TABLE $TABLE_CART (
                productId TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                brand TEXT NOT NULL,
                price REAL NOT NULL,
                unit TEXT NOT NULL,
                description TEXT NOT NULL,
                quantity INTEGER NOT NULL
            );
        """.trimIndent())

        // Orders
        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                id TEXT PRIMARY KEY,
                itemsJson TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                status TEXT NOT NULL,
                deliveryAddress TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            );
        """.trimIndent())

        // Weather Cache
        db.execSQL("""
            CREATE TABLE $TABLE_WEATHER (
                id INTEGER PRIMARY KEY,
                locationName TEXT NOT NULL,
                temperature REAL NOT NULL,
                humidity INTEGER NOT NULL,
                windSpeed REAL NOT NULL,
                conditionDescription TEXT NOT NULL,
                precipitation REAL NOT NULL,
                advisoryNotice TEXT NOT NULL,
                forecastJson TEXT NOT NULL,
                lastUpdated INTEGER NOT NULL
            );
        """.trimIndent())

        // Chat messages
        db.execSQL("""
            CREATE TABLE $TABLE_CHAT (
                id TEXT PRIMARY KEY,
                sender TEXT NOT NULL,
                text TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                language TEXT NOT NULL,
                isDemoResponse INTEGER NOT NULL
            );
        """.trimIndent())

        // Notifications
        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                category TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isRead INTEGER NOT NULL
            );
        """.trimIndent())

        // Preferences & Offline settings
        db.execSQL("""
            CREATE TABLE $TABLE_PREFERENCES (
                prefKey TEXT PRIMARY KEY,
                prefValue TEXT NOT NULL
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handles schema migrations
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PREFERENCES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WEATHER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECOMMENDATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SOIL_ASSESSMENT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DISEASE_ANALYSIS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CROPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FARMS")
        onCreate(db)
    }
}
