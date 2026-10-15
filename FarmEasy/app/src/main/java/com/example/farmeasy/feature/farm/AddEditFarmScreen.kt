package com.example.farmeasy.feature.farm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmeasy.ui.components.FormTextField
import com.example.farmeasy.ui.components.PrimaryButton
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFarmScreen(
    uiState: FarmUiState,
    onEvent: (FarmUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Farm Parcel", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                text = "Enter parcel specifications to link local soil data and crop sowing cycles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(
                value = uiState.farmNameInput,
                onValueChange = { onEvent(FarmUiEvent.FarmNameChanged(it)) },
                label = "Farm / Parcel Name",
                placeholder = "e.g. Shanti Farm - North Plot",
                leadingIcon = Icons.Default.Terrain
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = uiState.farmAreaInput,
                onValueChange = { onEvent(FarmUiEvent.FarmAreaChanged(it)) },
                label = "Cultivable Area (in Acres)",
                placeholder = "e.g. 3.5",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = Icons.Default.SquareFoot
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = uiState.soilTypeInput,
                onValueChange = { onEvent(FarmUiEvent.SoilTypeChanged(it)) },
                label = "Soil Type",
                placeholder = "e.g. Black Cotton Soil / Red Sandy Loam",
                leadingIcon = Icons.Default.Layers
            )

            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = uiState.locationNameInput,
                onValueChange = { onEvent(FarmUiEvent.LocationChanged(it)) },
                label = "Village / Tehsil Location",
                placeholder = "e.g. Haveli, Pune",
                leadingIcon = Icons.Default.LocationOn
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Save Farm Parcel",
                onClick = {
                    onEvent(FarmUiEvent.SaveFarm)
                    onNavigateBack()
                },
                isLoading = uiState.isLoading
            )
        }
    }
}
