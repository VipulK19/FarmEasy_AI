package com.example.farmeasy.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.navigation.Screen
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            FarmEasyTopBar(
                title = "FarmEasy",
                isDemoMode = uiState.isDemoMode,
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Notifications.route) }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts")
                    }
                    IconButton(onClick = { onNavigate(Screen.VoiceAssistant.route) }) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Assistant")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium)
        ) {
            // Greeting & Farmer Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Namaste, ${uiState.farmer?.name?.split(" ")?.firstOrNull() ?: "Kisan"}!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.farmer?.district ?: "Pune"}, ${uiState.farmer?.state ?: "Maharashtra"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FarmerAvatar(
                    name = uiState.farmer?.name ?: "Kisan",
                    size = 52.dp,
                    modifier = Modifier.clickable { onNavigate(Screen.Profile.route) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather Card
            WeatherCard(
                weather = uiState.weather,
                onRefresh = { onEvent(DashboardUiEvent.RefreshData) },
                onClick = { onNavigate(Screen.Weather.route) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Section
            Text(
                text = "Smart Advisory Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Detect Disease",
                    subtitle = "AI Leaf Scan",
                    icon = Icons.Default.DocumentScanner,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.DiseaseDetection.route) }
                )
                QuickActionCard(
                    title = "Soil Health",
                    subtitle = "NPK Assessment",
                    icon = Icons.Default.Science,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.SoilAssessment.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Ask AI Kisan",
                    subtitle = "Voice & Text",
                    icon = Icons.Default.Psychology,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Chatbot.route) }
                )
                QuickActionCard(
                    title = "Govt Schemes",
                    subtitle = "Subsidies & Loans",
                    icon = Icons.Default.AccountBalance,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Schemes.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // My Farm Overview Card
            FarmEasyCard(
                onClick = { onNavigate(Screen.FarmList.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Farm Parcels (${uiState.farms.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to Farm",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalArea = uiState.farms.sumOf { it.areaInAcres }
                Text(
                    text = "Total cultivated acreage: ${"%.1f".format(totalArea)} Acres across ${uiState.farms.size} registered plots.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recommended Agri-Actions
            if (uiState.recommendations.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Prioritized Field Advisory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigate(Screen.Recommendations.route) }) {
                        Text("View All")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                uiState.recommendations.take(2).forEach { rec ->
                    RecommendationCard(recommendation = rec)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Legal & Agronomic disclaimer
            Surface(
                shape = RoundedCornerShape(Spacing.cornerRadiusSmall),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Decision-Support Notice: Predictions and advisory are generated via AI and agronomic models. Always observe certified pesticide label safety guidelines.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(Spacing.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconTint.copy(alpha = 0.8f)
                )
            }
        }
    }
}
