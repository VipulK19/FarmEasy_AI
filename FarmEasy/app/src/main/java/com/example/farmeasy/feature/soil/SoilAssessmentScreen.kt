package com.example.farmeasy.feature.soil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.SoilAssessment
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing
import com.example.farmeasy.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilAssessmentScreen(
    uiState: SoilUiState,
    onEvent: (SoilUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soil Health & NPK", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.history.isNotEmpty()) {
                        IconButton(onClick = { onEvent(SoilUiEvent.ClearHistory) }) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear History")
                        }
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
            Text(
                text = "Enter soil test parameters (from your Soil Health Card or lab report) to generate tailored fertilizer doses.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parameter Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormTextField(
                    value = uiState.phText,
                    onValueChange = { onEvent(SoilUiEvent.PhChanged(it)) },
                    label = "Soil pH (3.0 - 10.0)",
                    placeholder = "6.5",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                FormTextField(
                    value = uiState.moistureText,
                    onValueChange = { onEvent(SoilUiEvent.MoistureChanged(it)) },
                    label = "Moisture %",
                    placeholder = "25",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormTextField(
                    value = uiState.nitrogenText,
                    onValueChange = { onEvent(SoilUiEvent.NitrogenChanged(it)) },
                    label = "Nitrogen (N) kg/ha",
                    placeholder = "180",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                FormTextField(
                    value = uiState.phosphorusText,
                    onValueChange = { onEvent(SoilUiEvent.PhosphorusChanged(it)) },
                    label = "Phosphorus (P) kg/ha",
                    placeholder = "35",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormTextField(
                    value = uiState.potassiumText,
                    onValueChange = { onEvent(SoilUiEvent.PotassiumChanged(it)) },
                    label = "Potassium (K) kg/ha",
                    placeholder = "220",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                FormTextField(
                    value = uiState.intendedCrop,
                    onValueChange = { onEvent(SoilUiEvent.IntendedCropChanged(it)) },
                    label = "Intended Crop",
                    placeholder = "Wheat",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = uiState.soilType,
                onValueChange = { onEvent(SoilUiEvent.SoilTypeChanged(it)) },
                label = "Soil Classification",
                placeholder = "e.g. Medium Black Soil / Red Loam"
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Calculate Fertility Index & Fertilizer Plan",
                onClick = { onEvent(SoilUiEvent.AssessClicked) },
                isLoading = uiState.isAssessing,
                icon = Icons.Default.Science
            )

            // Current Assessment Result Section
            if (uiState.currentAssessment != null) {
                Spacer(modifier = Modifier.height(24.dp))
                SoilResultSection(
                    assessment = uiState.currentAssessment,
                    onDismiss = { onEvent(SoilUiEvent.ClearCurrentAssessment) }
                )
            }

            // History Section
            if (uiState.history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Previous Soil Assessments (${uiState.history.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                uiState.history.forEach { item ->
                    FarmEasyCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Crop: ${item.parameters.intendedCrop}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.fertilityScore}/100 Index",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "pH ${item.parameters.ph} | N: ${item.parameters.nitrogen} | P: ${item.parameters.phosphorus} | K: ${item.parameters.potassium}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.fertilityClass,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SoilResultSection(
    assessment: SoilAssessment,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "FERTILITY REPORT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Gauge Visualization
            SoilNpkGauge(
                score = assessment.fertilityScore,
                parameters = assessment.parameters
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = assessment.fertilityClass,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Organic Carbon Status: ${assessment.organicCarbonStatus}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Agronomic Nutrient Management Plan:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = assessment.fertilizerRecommendation,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Highly Compatible Crop Rotations:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                assessment.recommendedCrops.forEach { crop ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(crop) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = assessment.safetyNotice,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
