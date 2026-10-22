package com.example.farmeasy.feature.disease

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.CropDiseaseAnalysis
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing
import com.example.farmeasy.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    uiState: DiseaseUiState,
    onEvent: (DiseaseUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var expandedCropDropdown by remember { mutableStateOf(false) }
    val cropList = listOf("Tomato", "Cotton", "Rice", "Wheat", "Potato", "Soybean")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onEvent(DiseaseUiEvent.ImageSelected(uri?.toString()))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crop Disease Detection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.history.isNotEmpty()) {
                        IconButton(onClick = { onEvent(DiseaseUiEvent.ClearHistory) }) {
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
            // Instruction
            Text(
                text = "Capture or select a clear photo of an infected leaf or stem to diagnose plant pathology.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Crop Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCropDropdown,
                onExpandedChange = { expandedCropDropdown = !expandedCropDropdown }
            ) {
                OutlinedTextField(
                    value = uiState.selectedCrop,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selected Crop Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCropDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(Spacing.cornerRadiusMedium)
                )
                ExposedDropdownMenu(
                    expanded = expandedCropDropdown,
                    onDismissRequest = { expandedCropDropdown = false }
                ) {
                    cropList.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text(crop) },
                            onClick = {
                                onEvent(DiseaseUiEvent.CropChanged(crop))
                                expandedCropDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Selection Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(Spacing.cornerRadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(Spacing.cornerRadiusMedium))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (uiState.imageUriString != null) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                        contentDescription = "Upload Photo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.imageUriString != null) "Photo Selected (Tap to change)" else "Tap to choose image from Gallery or Camera",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "PNG, JPG up to 10MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Run AI Plant Pathology Diagnosis",
                onClick = { onEvent(DiseaseUiEvent.AnalyzeClicked) },
                isLoading = uiState.isAnalyzing,
                icon = Icons.Default.Search
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Current Diagnosis Result Section
            if (uiState.currentAnalysis != null) {
                Spacer(modifier = Modifier.height(24.dp))
                DiagnosisResultSection(
                    analysis = uiState.currentAnalysis,
                    onDismiss = { onEvent(DiseaseUiEvent.ClearCurrentResult) }
                )
            }

            // Historical Analyses Section
            if (uiState.history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Previous Disease Assessments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                uiState.history.forEach { item ->
                    DiseaseResultCard(analysis = item)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagnosisResultSection(
    analysis: CropDiseaseAnalysis,
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
                        text = "DIAGNOSIS RESULT",
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

            Text(
                text = analysis.topPrediction.conditionName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (analysis.topPrediction.scientificName.isNotBlank()) {
                Text(
                    text = "Pathogen: ${analysis.topPrediction.scientificName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Confidence Level:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${(analysis.topPrediction.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { analysis.topPrediction.confidence },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Symptoms
            Text(text = "Visual Symptoms:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = analysis.topPrediction.symptoms, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Biological management
            Text(text = "Biological & Organic Treatment:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = analysis.topPrediction.biologicalTreatment, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Chemical treatment
            Text(text = "Recommended Chemical Treatment:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = analysis.topPrediction.chemicalTreatment, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Preventive measures
            Text(text = "Preventive & Cultural Practices:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = analysis.topPrediction.preventiveMeasures, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // Safety Warning
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Always adhere to CIB&RC approved labels, protective gear, and pre-harvest intervals before chemical application.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
