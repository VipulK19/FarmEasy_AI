package com.example.farmeasy.feature.farm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.Crop
import com.example.farmeasy.domain.model.Farm
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing

@Composable
fun FarmListScreen(
    uiState: FarmUiState,
    onEvent: (FarmUiEvent) -> Unit,
    onNavigateToAddFarm: () -> Unit
) {
    if (uiState.farmToDelete != null) {
        ConfirmationDialog(
            title = "Delete Farm Parcel?",
            message = "Are you sure you want to delete '${uiState.farmToDelete.name}' and all associated crop records?",
            confirmText = "Delete",
            onConfirm = { onEvent(FarmUiEvent.DeleteFarmConfirmed) },
            onDismiss = { onEvent(FarmUiEvent.DismissDeleteFarm) }
        )
    }

    if (uiState.cropToDelete != null) {
        ConfirmationDialog(
            title = "Delete Crop Record?",
            message = "Are you sure you want to remove '${uiState.cropToDelete.cropName}'?",
            confirmText = "Delete",
            onConfirm = { onEvent(FarmUiEvent.DeleteCropConfirmed) },
            onDismiss = { onEvent(FarmUiEvent.DismissDeleteCrop) }
        )
    }

    if (uiState.showAddCropDialog && uiState.selectedFarm != null) {
        AddCropDialog(
            farmName = uiState.selectedFarm.name,
            cropName = uiState.cropNameInput,
            cropVariety = uiState.cropVarietyInput,
            cropStage = uiState.cropStageInput,
            cropNotes = uiState.cropNotesInput,
            errorMessage = uiState.errorMessage,
            onCropNameChange = { onEvent(FarmUiEvent.CropNameChanged(it)) },
            onCropVarietyChange = { onEvent(FarmUiEvent.CropVarietyChanged(it)) },
            onCropStageChange = { onEvent(FarmUiEvent.CropStageChanged(it)) },
            onCropNotesChange = { onEvent(FarmUiEvent.CropNotesChanged(it)) },
            onSave = { onEvent(FarmUiEvent.SaveCrop) },
            onDismiss = { onEvent(FarmUiEvent.DismissAddCropModal) }
        )
    }

    Scaffold(
        topBar = {
            FarmEasyTopBar(
                title = "My Farm Parcels",
                actions = {
                    IconButton(onClick = onNavigateToAddFarm) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Farm")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddFarm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Farm Parcel")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.medium)
        ) {
            if (uiState.farms.isEmpty()) {
                EmptyStateView(
                    title = "No Farms Registered",
                    description = "Add your agricultural land parcel to record crops, soil types, and tailored spraying advisories.",
                    actionText = "Register First Farm",
                    onAction = onNavigateToAddFarm
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.farms, key = { it.id }) { farm ->
                        FarmParcelCard(
                            farm = farm,
                            isSelected = farm.id == uiState.selectedFarm?.id,
                            crops = if (farm.id == uiState.selectedFarm?.id) uiState.cropsForSelectedFarm else emptyList(),
                            onSelect = { onEvent(FarmUiEvent.FarmSelected(farm)) },
                            onDelete = { onEvent(FarmUiEvent.ConfirmDeleteFarm(farm)) },
                            onAddCrop = { onEvent(FarmUiEvent.ShowAddCropModal) },
                            onDeleteCrop = { crop -> onEvent(FarmUiEvent.ConfirmDeleteCrop(crop)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmParcelCard(
    farm: Farm,
    isSelected: Boolean,
    crops: List<Crop>,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onAddCrop: () -> Unit,
    onDeleteCrop: (Crop) -> Unit
) {
    FarmEasyCard(onClick = onSelect) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = farm.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${farm.areaInAcres} Acres • ${farm.soilType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Farm", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Location: ${farm.locationName} (${"%.3f".format(farm.latitude)}, ${"%.3f".format(farm.longitude)})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cultivated Crops (${crops.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onAddCrop) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Crop")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (crops.isEmpty()) {
                Text(
                    text = "No active crops recorded. Tap 'Add Crop' to register a crop cycle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                crops.forEach { crop ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${crop.cropName}${if (crop.variety.isNotBlank()) " (${crop.variety})" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Growth Stage: ${crop.growthStage}${if (crop.notes.isNotBlank()) " • ${crop.notes}" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCrop(crop) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Crop",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCropDialog(
    farmName: String,
    cropName: String,
    cropVariety: String,
    cropStage: String,
    cropNotes: String,
    errorMessage: String?,
    onCropNameChange: (String) -> Unit,
    onCropVarietyChange: (String) -> Unit,
    onCropStageChange: (String) -> Unit,
    onCropNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Crop to $farmName", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                FormTextField(
                    value = cropName,
                    onValueChange = onCropNameChange,
                    label = "Crop Name",
                    placeholder = "e.g. Wheat / Paddy / Tomato / Cotton"
                )
                Spacer(modifier = Modifier.height(8.dp))
                FormTextField(
                    value = cropVariety,
                    onValueChange = onCropVarietyChange,
                    label = "Variety (Optional)",
                    placeholder = "e.g. Lok-1 / JS 335 / Bt Cotton"
                )
                Spacer(modifier = Modifier.height(8.dp))
                FormTextField(
                    value = cropStage,
                    onValueChange = onCropStageChange,
                    label = "Growth Stage",
                    placeholder = "e.g. Sowing / Vegetative / Flowering"
                )
                Spacer(modifier = Modifier.height(8.dp))
                FormTextField(
                    value = cropNotes,
                    onValueChange = onCropNotesChange,
                    label = "Field Notes (Optional)",
                    placeholder = "e.g. Basal DAP applied"
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Add Crop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
