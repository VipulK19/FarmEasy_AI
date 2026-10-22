package com.example.farmeasy.feature.dashboard

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.navigation.Screen
import com.example.farmeasy.ui.components.FarmEasyCard
import com.example.farmeasy.ui.components.FarmEasyTopBar
import com.example.farmeasy.ui.theme.Spacing

data class ServiceItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ServicesScreen(
    onNavigate: (String) -> Unit
) {
    val cropCareServices = listOf(
        ServiceItem("Crop Disease Detection", "Instant plant pathology diagnosis via leaf photos", Icons.Default.DocumentScanner, Screen.DiseaseDetection.route),
        ServiceItem("Soil Health & NPK", "Fertility scoring and tailored nutrient management", Icons.Default.Science, Screen.SoilAssessment.route),
        ServiceItem("Weather Forecast", "7-day precipitation, wind, and spray window advisories", Icons.Default.Cloud, Screen.Weather.route)
    )

    val marketServices = listOf(
        ServiceItem("Govt Schemes & Subsidies", "Explore PM-KISAN, PMFBY, and machinery subsidies", Icons.Default.AccountBalance, Screen.Schemes.route),
        ServiceItem("Agricultural Loans & Credit", "KCC crop loans, tractor financing, interest subvention", Icons.Default.Payment, Screen.Loans.route),
        ServiceItem("Kisan Marketplace", "Order certified fertilizers, bio-pesticides, and tools", Icons.Default.ShoppingBag, Screen.Marketplace.route),
        ServiceItem("Agri Dealer Locator", "Find nearby certified fertilizer, seed, and tool stores", Icons.Default.Store, Screen.Dealers.route)
    )

    val assistantServices = listOf(
        ServiceItem("Voice Assistant", "Hands-free speech-to-text agricultural inquiries", Icons.Default.Mic, Screen.VoiceAssistant.route),
        ServiceItem("Advisory Notifications", "Real-time alerts, seasonal reminders, and weather notices", Icons.Default.Notifications, Screen.Notifications.route)
    )

    Scaffold(
        topBar = {
            FarmEasyTopBar(title = "Agricultural Services")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium)
        ) {
            Text(
                text = "Farming & Crop Care",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            cropCareServices.forEach { item ->
                ServiceCard(item = item, onClick = { onNavigate(item.route) })
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Marketplace & Schemes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            marketServices.forEach { item ->
                ServiceCard(item = item, onClick = { onNavigate(item.route) })
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Assistant & Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            assistantServices.forEach { item ->
                ServiceCard(item = item, onClick = { onNavigate(item.route) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ServiceCard(
    item: ServiceItem,
    onClick: () -> Unit
) {
    FarmEasyCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
