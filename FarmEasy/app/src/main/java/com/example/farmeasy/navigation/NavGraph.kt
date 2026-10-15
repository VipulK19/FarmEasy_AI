package com.example.farmeasy.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.farmeasy.di.AppContainer
import com.example.farmeasy.feature.auth.*
import com.example.farmeasy.feature.chatbot.*
import com.example.farmeasy.feature.dashboard.*
import com.example.farmeasy.feature.dealer.*
import com.example.farmeasy.feature.disease.*
import com.example.farmeasy.feature.farm.*
import com.example.farmeasy.feature.marketplace.*
import com.example.farmeasy.feature.notifications.*
import com.example.farmeasy.feature.profile.*
import com.example.farmeasy.feature.recommendations.*
import com.example.farmeasy.feature.schemes.*
import com.example.farmeasy.feature.soil.*
import com.example.farmeasy.feature.voice.*
import com.example.farmeasy.feature.weather.*
import com.example.farmeasy.ui.components.FarmEasyBottomNavigation
import com.example.farmeasy.ui.components.NetworkStatusIndicator

@Composable
fun FarmEasyNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isOnline by appContainer.networkMonitor.isOnline.collectAsStateWithLifecycle(initialValue = true)

    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Chatbot.route,
        Screen.FarmList.route,
        Screen.Services.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                FarmEasyBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NetworkStatusIndicator(isOnline = isOnline)

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    // Auth
                    composable(Screen.Login.route) {
                        val viewModel = remember {
                            AuthViewModel(
                                appContainer.loginUseCase,
                                appContainer.registerUseCase,
                                appContainer.loginDemoUseCase
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        LoginScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                            onLoginSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Register.route) {
                        val viewModel = remember {
                            AuthViewModel(
                                appContainer.loginUseCase,
                                appContainer.registerUseCase,
                                appContainer.loginDemoUseCase
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        RegisterScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() },
                            onRegisterSuccess = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // Dashboard
                    composable(Screen.Home.route) {
                        val viewModel = remember {
                            DashboardViewModel(
                                appContainer.authRepository,
                                appContainer.farmRepository,
                                appContainer.weatherRepository,
                                appContainer.recommendationRepository,
                                appContainer.diseaseRepository,
                                appContainer.soilRepository
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        DashboardScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }

                    // AI Chatbot
                    composable(Screen.Chatbot.route) {
                        val viewModel = remember {
                            ChatViewModel(
                                appContainer.sendChatMessageUseCase,
                                appContainer.preferencesRepository,
                                appContainer.audioSpeechManager
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        ChatScreen(
                            uiState = uiState,
                            suggestedQuestions = viewModel.suggestedQuestions,
                            onEvent = viewModel::onEvent
                        )
                    }

                    // Farm & Crops
                    composable(Screen.FarmList.route) {
                        val viewModel = remember { FarmViewModel(appContainer.manageFarmUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        FarmListScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateToAddFarm = { navController.navigate(Screen.AddEditFarm.route) }
                        )
                    }

                    composable(Screen.AddEditFarm.route) {
                        val viewModel = remember { FarmViewModel(appContainer.manageFarmUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        AddEditFarmScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Services Hub
                    composable(Screen.Services.route) {
                        ServicesScreen(
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }

                    // Crop Disease Detection
                    composable(Screen.DiseaseDetection.route) {
                        val viewModel = remember { DiseaseViewModel(appContainer.analyzeDiseaseUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        DiseaseDetectionScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Soil Health
                    composable(Screen.SoilAssessment.route) {
                        val viewModel = remember { SoilViewModel(appContainer.assessSoilUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        SoilAssessmentScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Recommendations
                    composable(Screen.Recommendations.route) {
                        val viewModel = remember { RecommendationViewModel(appContainer.recommendationRepository) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        RecommendationScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Schemes
                    composable(Screen.Schemes.route) {
                        val viewModel = remember { SchemeViewModel(appContainer.schemeRepository) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        SchemesScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Loans
                    composable(Screen.Loans.route) {
                        val viewModel = remember { SchemeViewModel(appContainer.schemeRepository) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        LoansScreen(
                            uiState = uiState,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Dealer Locator
                    composable(Screen.Dealers.route) {
                        val viewModel = remember { DealerViewModel(appContainer.dealerRepository) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        DealerLocatorScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Marketplace
                    composable(Screen.Marketplace.route) {
                        val viewModel = remember { MarketplaceViewModel(appContainer.manageMarketplaceUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        MarketplaceScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                        )
                    }

                    composable(Screen.Cart.route) {
                        val viewModel = remember { MarketplaceViewModel(appContainer.manageMarketplaceUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        CartScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Weather
                    composable(Screen.Weather.route) {
                        val viewModel = remember { WeatherViewModel(appContainer.getWeatherUseCase) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        WeatherScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Voice Assistant
                    composable(Screen.VoiceAssistant.route) {
                        val viewModel = remember {
                            VoiceViewModel(
                                appContainer.audioSpeechManager,
                                appContainer.sendChatMessageUseCase,
                                appContainer.preferencesRepository
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        VoiceAssistantScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Notifications
                    composable(Screen.Notifications.route) {
                        val viewModel = remember { NotificationViewModel(appContainer.notificationRepository) }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        NotificationsScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    // Profile
                    composable(Screen.Profile.route) {
                        val viewModel = remember {
                            ProfileViewModel(
                                appContainer.authRepository,
                                appContainer.preferencesRepository
                            )
                        }
                        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                        ProfileScreen(
                            uiState = uiState,
                            onEvent = viewModel::onEvent,
                            onLoggedOut = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
