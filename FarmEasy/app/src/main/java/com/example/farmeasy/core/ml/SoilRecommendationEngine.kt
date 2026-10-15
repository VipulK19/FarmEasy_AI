package com.example.farmeasy.core.ml

import com.example.farmeasy.domain.model.SoilAssessment
import com.example.farmeasy.domain.model.SoilParameters
import kotlin.math.roundToInt

class SoilRecommendationEngine {

    /**
     * Assesses soil parameters using ICAR (Indian Council of Agricultural Research)
     * standard agronomic rating charts and decision tree algorithms.
     */
    fun assess(params: SoilParameters): SoilAssessment {
        val phScore = when {
            params.ph in 6.2..7.8 -> 30 // Optimal neutral
            params.ph in 5.5..6.1 -> 20 // Slightly acidic
            params.ph in 7.9..8.5 -> 20 // Moderately alkaline
            params.ph < 5.5 -> 10       // Strongly acidic
            else -> 10                  // Strongly alkaline
        }

        // Nitrogen status (kg/ha): Low < 140, Medium 140-280, High > 280
        val nScore = when {
            params.nitrogen < 140.0 -> 10
            params.nitrogen in 140.0..280.0 -> 25
            else -> 22 // Excess N can cause vegetative overgrowth / pest susceptibility
        }

        // Phosphorus status (kg/ha): Low < 23, Medium 23-56, High > 56
        val pScore = when {
            params.phosphorus < 23.0 -> 8
            params.phosphorus in 23.0..56.0 -> 25
            else -> 23
        }

        // Potassium status (kg/ha): Low < 135, Medium 135-335, High > 335
        val kScore = when {
            params.potassium < 135.0 -> 7
            params.potassium in 135.0..335.0 -> 20
            else -> 18
        }

        val totalScore = (phScore + nScore + pScore + kScore).coerceIn(10, 100)

        val fertilityClass = when {
            totalScore >= 80 -> "High Fertility (Well-Balanced)"
            totalScore >= 55 -> "Medium Fertility (Needs Targeted Nutrients)"
            params.ph < 5.5 -> "Low Fertility (Acidic Soil)"
            params.ph > 8.2 -> "Low Fertility (Alkaline / Calcareous Soil)"
            else -> "Low Fertility (Deficient in Macronutrients)"
        }

        val organicCarbonStatus = when {
            totalScore >= 75 -> "Adequate (>0.75%)"
            totalScore >= 50 -> "Medium (0.50% - 0.75%)"
            else -> "Low (<0.50%)"
        }

        val recommendations = buildFertilizerPlan(params)
        val suitableCrops = getSuitableCrops(params)

        return SoilAssessment(
            parameters = params,
            fertilityScore = totalScore,
            fertilityClass = fertilityClass,
            organicCarbonStatus = organicCarbonStatus,
            fertilizerRecommendation = recommendations,
            recommendedCrops = suitableCrops,
            isDemoResult = false
        )
    }

    private fun buildFertilizerPlan(p: SoilParameters): String {
        val steps = mutableListOf<String>()

        // pH management
        if (p.ph < 6.0) {
            steps.add("Apply agricultural lime or dolomite at 250-500 kg/acre prior to sowing to neutralize soil acidity.")
        } else if (p.ph > 8.0) {
            steps.add("Apply agricultural gypsum at 400-600 kg/acre and incorporate farmyard manure (FYM) to counter soil sodicity.")
        }

        // Nitrogen management
        if (p.nitrogen < 140.0) {
            steps.add("Nitrogen is deficient. Apply 35-45 kg Urea/acre in 3 split doses (basal, tillering/vegetative, and pre-flowering).")
        } else if (p.nitrogen > 280.0) {
            steps.add("Nitrogen is high. Reduce standard urea application by 25% to prevent lodging and fungal infestation.")
        } else {
            steps.add("Nitrogen is optimal. Maintain standard recommended basal dose of NPK (10:26:26 or 12:32:16).")
        }

        // Phosphorus management
        if (p.phosphorus < 23.0) {
            steps.add("Phosphorus is low. Apply 50 kg DAP (Di-ammonium Phosphate) or Single Super Phosphate (SSP) at basal root zone.")
        }

        // Potassium management
        if (p.potassium < 135.0) {
            steps.add("Potassium is low. Apply 20-30 kg MOP (Muriate of Potash) per acre to improve grain filling and drought resistance.")
        }

        // Organic booster
        steps.add("Incorporate 2-3 tonnes of decomposed Farmyard Manure (FYM) or vermicompost per acre to enhance microbial biomass.")

        return steps.joinToString("\n\n")
    }

    private fun getSuitableCrops(p: SoilParameters): List<String> {
        val crops = mutableListOf<String>()
        if (p.ph in 6.0..7.5) {
            crops.addAll(listOf("Wheat", "Paddy", "Maize", "Chickpea (Gram)", "Soybean"))
        }
        if (p.ph > 7.5) {
            crops.addAll(listOf("Cotton", "Barley", "Mustard", "Sugarcane"))
        }
        if (p.ph < 6.0) {
            crops.addAll(listOf("Potato", "Tea", "Sweet Potato", "Ginger"))
        }
        if (p.nitrogen > 200.0) {
            crops.add("High-yielding hybrid Maize")
        }
        return crops.distinct().take(5)
    }
}
