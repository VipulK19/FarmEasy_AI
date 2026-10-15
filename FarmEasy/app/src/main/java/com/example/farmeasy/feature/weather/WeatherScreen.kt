package com.example.farmeasy.feature.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.ui.components.FarmEasyCard
import com.example.farmeasy.ui.components.WeatherCard
import com.example.farmeasy.ui.components.WeatherTrendChart
import com.example.farmeasy.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onEvent: (WeatherUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val locations = listOf(
        Triple("Pune (Main Farm)", 18.5204, 73.8567),
        Triple("Nashik (Grapes Belt)", 19.9975, 73.7898),
        Triple("Nagpur (Orange Belt)", 21.1458, 79.0882),
        Triple("Satara (Sugarcane)", 17.6805, 74.0183)
    )

    val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(uiState.weather.lastUpdated))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather & Spray Window", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(WeatherUiEvent.Refresh) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Location Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(locations) { loc ->
                        FilterChip(
                            selected = uiState.selectedLocationName == loc.first,
                            onClick = { onEvent(WeatherUiEvent.LocationSelected(loc.first, loc.second, loc.third)) },
                            label = { Text(loc.first) }
                        )
                    }
                }
            }

            // Main Weather Card
            item {
                WeatherCard(
                    weather = uiState.weather,
                    onRefresh = { onEvent(WeatherUiEvent.Refresh) }
                )
            }

            // Cached timestamp info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (uiState.weather.isCached) "Status: Local Offline Cache" else "Status: Live Open-Meteo Feed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Updated: $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Spray Window Card
            item {
                FarmEasyCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agrochemical Spray Window Advisory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.weather.precipitationMm > 2.0 || uiState.weather.windSpeedKmh > 18.0)
                            "UNFAVORABLE: High wind or rain forecasted. Spraying now may cause chemical runoff or drift loss."
                        else
                            "FAVORABLE: Calm wind (<15 km/h) and dry conditions. Excellent window for systemic fungicide or pesticide foliar spray.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Compose Canvas Temperature Chart
            if (uiState.weather.forecast.isNotEmpty()) {
                item {
                    FarmEasyCard {
                        WeatherTrendChart(forecast = uiState.weather.forecast)
                    }
                }
            }

            // Daily Forecast Items
            if (uiState.weather.forecast.isNotEmpty()) {
                item {
                    Text(
                        text = "Daily Forecast (Next 7 Days)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.weather.forecast) { item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.dayOfWeek,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = item.weatherCondition,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${item.maxTemp.toInt()}° / ${item.minTemp.toInt()}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
