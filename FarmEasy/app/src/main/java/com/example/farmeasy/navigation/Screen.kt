package com.example.farmeasy.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Chatbot : Screen("chatbot")
    object FarmList : Screen("farm_list")
    object AddEditFarm : Screen("add_edit_farm")
    object Services : Screen("services")
    object Profile : Screen("profile")
    object DiseaseDetection : Screen("disease_detection")
    object SoilAssessment : Screen("soil_assessment")
    object Recommendations : Screen("recommendations")
    object Schemes : Screen("schemes")
    object Loans : Screen("loans")
    object Dealers : Screen("dealers")
    object Marketplace : Screen("marketplace")
    object Cart : Screen("cart")
    object Weather : Screen("weather")
    object VoiceAssistant : Screen("voice_assistant")
    object Notifications : Screen("notifications")
}
