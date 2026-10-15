package com.example.farmeasy.data.sample

import com.example.farmeasy.domain.model.*

object SampleData {

    val sampleFarmer = Farmer(
        id = "demo_farmer_001",
        name = "Ramesh Baburao Patil",
        phone = "+91 98765 43210",
        email = "ramesh.patil@farmeasy.demo",
        state = "Maharashtra",
        district = "Pune",
        preferredLanguage = "English",
        preferredCrops = listOf("Soybean", "Wheat", "Tomato"),
        isDemoUser = true
    )

    val sampleFarms = listOf(
        Farm(
            id = "farm_001",
            name = "Shanti Farm - Main Plot",
            areaInAcres = 4.5,
            soilType = "Medium Black Soil",
            locationName = "Haveli, Pune",
            latitude = 18.5204,
            longitude = 73.8567,
            createdAt = System.currentTimeMillis() - (180L * 24 * 3600 * 1000)
        ),
        Farm(
            id = "farm_002",
            name = "Riverbed Vegetable Parcel",
            areaInAcres = 2.0,
            soilType = "Alluvial Loam",
            locationName = "Shirur, Pune",
            latitude = 18.8286,
            longitude = 74.3789,
            createdAt = System.currentTimeMillis() - (90L * 24 * 3600 * 1000)
        )
    )

    val sampleCrops = listOf(
        Crop(
            id = "crop_001",
            farmId = "farm_001",
            cropName = "Soybean",
            variety = "JS 335",
            growthStage = "Pod Formation",
            notes = "Good canopy development. Monitored for pod borer."
        ),
        Crop(
            id = "crop_002",
            farmId = "farm_001",
            cropName = "Wheat",
            variety = "HD 2967",
            growthStage = "Vegetative",
            notes = "First irrigation complete with urea application."
        ),
        Crop(
            id = "crop_003",
            farmId = "farm_002",
            cropName = "Tomato",
            variety = "Abhinav F1",
            growthStage = "Flowering & Fruit Setting",
            notes = "Staking completed, drip fertigation twice weekly."
        )
    )

    val sampleSchemes = listOf(
        Scheme(
            id = "scheme_pm_kisan",
            title = "Pradhan Mantri Kisan Samman Nidhi (PM-KISAN)",
            ministry = "Ministry of Agriculture & Farmers Welfare, GoI",
            benefitSummary = "Direct income support of ₹6,000 per year in three equal installments of ₹2,000 directly into Aadhaar-seeded bank accounts.",
            eligibility = "All landholding farmer families having cultivable landholding in their names (subject to exclusion criteria for institutional landholders).",
            requiredDocuments = listOf("Aadhaar Card", "Land Ownership Documents (7/12 extract or RoR)", "Active Bank Passbook", "Mobile Number"),
            applicationSteps = "Apply via PM-KISAN portal (pmkisan.gov.in), Common Service Centres (CSC), or through State Agriculture Nodal Officers.",
            officialUrl = "https://pmkisan.gov.in",
            lastVerifiedDate = "2026-03-15",
            category = "Direct Financial Support"
        ),
        Scheme(
            id = "scheme_pmfby",
            title = "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
            ministry = "Ministry of Agriculture & Farmers Welfare, GoI",
            benefitSummary = "Comprehensive crop insurance against non-preventable natural risks from pre-sowing to post-harvest at minimal premium (2% Kharif, 1.5% Rabi, 5% Commercial/Horticultural crops).",
            eligibility = "All farmers including sharecroppers and tenant farmers growing notified crops in notified areas.",
            requiredDocuments = listOf("Land Record (7/12 / Khasra)", "Sowing Certificate / Declaration", "Aadhaar Card", "Bank Account Details"),
            applicationSteps = "Enroll through National Crop Insurance Portal (pmfby.gov.in), authorized banks, or primary agricultural credit societies (PACS) before cut-off dates.",
            officialUrl = "https://pmfby.gov.in",
            lastVerifiedDate = "2026-02-28",
            category = "Crop Insurance"
        ),
        Scheme(
            id = "scheme_smam",
            title = "Sub-Mission on Agricultural Mechanization (SMAM)",
            ministry = "Ministry of Agriculture & Farmers Welfare, GoI",
            benefitSummary = "40% to 50% subsidy on purchase of agricultural machinery, tractors, rotavators, power tillers, and drone sprayers.",
            eligibility = "Small and marginal farmers, SC/ST, women farmers are given priority subsidy allocation.",
            requiredDocuments = listOf("Aadhaar Card", "Land Title Copy", "Bank Passbook", "Caste Certificate (if applicable)", "Quotation from Authorized Dealer"),
            applicationSteps = "Register on Direct Benefit Transfer in Agriculture Mechanization portal (agrimachinery.nic.in).",
            officialUrl = "https://agrimachinery.nic.in",
            lastVerifiedDate = "2026-01-20",
            category = "Machinery Subsidy"
        ),
        Scheme(
            id = "scheme_pmksy",
            title = "PM Krishi Sinchayee Yojana (Per Drop More Crop)",
            ministry = "Department of Agriculture and Farmers Welfare",
            benefitSummary = "Up to 55% subsidy for small/marginal farmers and 45% for other farmers for installing drip and sprinkler micro-irrigation systems.",
            eligibility = "Farmers with assured water source and land possession.",
            requiredDocuments = listOf("Land Title/7-12 extract", "Water Source Certificate/Electricity Bill", "Aadhaar Card", "Bank Details"),
            applicationSteps = "Apply through state agriculture/horticulture department micro-irrigation online portal.",
            officialUrl = "https://pmksy.gov.in",
            lastVerifiedDate = "2026-02-10",
            category = "Micro Irrigation"
        )
    )

    val sampleLoans = listOf(
        LoanInformation(
            id = "loan_kcc",
            bankName = "State Bank of India (SBI)",
            schemeName = "Kisan Credit Card (KCC) Crop Loan",
            interestRate = "7.0% p.a. (Effectively 4.0% with prompt repayment subvention up to ₹3 Lakh)",
            maximumAmount = "Up to ₹3,00,000 without collateral for basic card; higher amounts based on scale of finance",
            tenure = "5 years revolving credit, reviewed annually",
            eligibilityCriteria = "All farmers, owner cultivators, tenant farmers, oral lessees & sharecroppers.",
            officialUrl = "https://sbi.co.in/web/agri-rural/agriculture-banking/crop-finance/kisan-credit-card"
        ),
        LoanInformation(
            id = "loan_tractor",
            bankName = "Bank of Baroda",
            schemeName = "Baroda Kisan Tractor Loan",
            interestRate = "8.75% - 9.50% p.a.",
            maximumAmount = "Up to 85% - 90% of tractor invoice cost",
            tenure = "Up to 7 years in half-yearly/annual installments linked to harvest",
            eligibilityCriteria = "Farmers possessing at least 2 acres of irrigated perennial land.",
            officialUrl = "https://www.bankofbaroda.in"
        )
    )

    val sampleDealers = listOf(
        Dealer(
            id = "dealer_001",
            businessName = "Kisan Agro Seva Kendra",
            dealerType = "Fertilizers & Hybrid Seeds",
            address = "Shop 12, APMC Market Yard, Pune - 411037",
            phone = "+91 94220 11223",
            latitude = 18.5204,
            longitude = 73.8567,
            distanceKm = 2.4,
            verified = true
        ),
        Dealer(
            id = "dealer_002",
            businessName = "Maharashtra Krishi Vikas Agency",
            dealerType = "Bio-Pesticides & Micro-Nutrients",
            address = "Opposite Panchayat Samiti, Haveli, Pune - 412207",
            phone = "+91 98230 44556",
            latitude = 18.5310,
            longitude = 73.8680,
            distanceKm = 4.1,
            verified = true
        ),
        Dealer(
            id = "dealer_003",
            businessName = "Mahindra Krishi Mitra Equipment",
            dealerType = "Tractor & Drip Irrigation Implements",
            address = "Pune-Solapur Highway, Hadapsar, Pune - 411028",
            phone = "+91 97650 77889",
            latitude = 18.5020,
            longitude = 73.9280,
            distanceKm = 7.8,
            verified = true
        )
    )

    val sampleProducts = listOf(
        MarketplaceProduct(
            id = "prod_001",
            name = "Neem Coated Urea (45kg)",
            category = "Fertilizers",
            brand = "KRIBHCO / IFFCO",
            price = 266.50,
            unit = "45kg Bag",
            description = "Subsidized Neem coated urea for slow nitrogen release and enhanced nutrient use efficiency.",
            inStock = true,
            rating = 4.8f
        ),
        MarketplaceProduct(
            id = "prod_002",
            name = "DAP 18:46:0 Di-Ammonium Phosphate",
            category = "Fertilizers",
            brand = "IFFCO",
            price = 1350.00,
            unit = "50kg Bag",
            description = "High-grade phosphatic fertilizer providing 18% nitrogen and 46% phosphorus for strong root foundation.",
            inStock = true,
            rating = 4.7f
        ),
        MarketplaceProduct(
            id = "prod_003",
            name = "Trichoderma Viride Bio-Fungicide",
            category = "Bio-Pesticides",
            brand = "Anand Agro Care",
            price = 240.00,
            unit = "1kg Pack",
            description = "Eco-friendly bio-fungicide for seed treatment and soil application to control root rot, wilt, and damping-off.",
            inStock = true,
            rating = 4.9f
        ),
        MarketplaceProduct(
            id = "prod_004",
            name = "Cold Pressed Neem Oil (10,000 ppm)",
            category = "Bio-Pesticides",
            brand = "EcoNeem",
            price = 380.00,
            unit = "1 Litre Bottle",
            description = "Broad-spectrum botanical pesticide effective against sucking pests, whiteflies, and aphids.",
            inStock = true,
            rating = 4.6f
        ),
        MarketplaceProduct(
            id = "prod_005",
            name = "16mm Inline Drip Lateral Roll (400m)",
            category = "Tools & Irrigation",
            brand = "Jain Irrigation",
            price = 2850.00,
            unit = "400m Roll",
            description = "Class II UV-stabilized cylindrical dripper lateral pipe with 40cm emitter spacing (2 LPH).",
            inStock = true,
            rating = 4.7f
        )
    )

    val sampleRecommendations = listOf(
        AgriculturalRecommendation(
            id = "rec_001",
            category = "Fertilizer",
            title = "Nitrogen Top-Dressing Window Open",
            actionSuggested = "Apply second split of Urea (30 kg/acre) in moist soil before evening.",
            rationale = "Wheat crop is entering 35-40 days tillering stage where vegetative biomass nitrogen demand peaks.",
            safetyPrecautions = "Do not apply on water-stressed dry soil or immediately before heavy rainfall.",
            confidenceScore = 0.92f
        ),
        AgriculturalRecommendation(
            id = "rec_002",
            category = "Disease Prevention",
            title = "Humid Weather: Fungal Spore Warning",
            actionSuggested = "Inspect tomato leaf undersides for water-soaked spots. Spray preventive Mancozeb (2g/L) if humidity stays >80%.",
            rationale = "High relative humidity combined with night temperatures of 16-20°C creates high risk for Early Blight.",
            safetyPrecautions = "Use protective mask and gloves while spraying. Observe 7-day pre-harvest interval.",
            confidenceScore = 0.88f
        )
    )

    val sampleNotifications = listOf(
        AppNotification(
            id = "notif_001",
            title = "Weather Advisory: Rain Expected",
            message = "Light showers predicted within 48 hours. Postpone foliar spraying and clear drainage furrows.",
            category = "Weather",
            timestamp = System.currentTimeMillis() - (2L * 3600 * 1000),
            isRead = false
        ),
        AppNotification(
            id = "notif_002",
            title = "PM-KISAN Installment Released",
            message = "Check Aadhaar seeded bank account for the latest ₹2,000 disbursement under PM-KISAN.",
            category = "Scheme",
            timestamp = System.currentTimeMillis() - (24L * 3600 * 1000),
            isRead = true
        )
    )
}
