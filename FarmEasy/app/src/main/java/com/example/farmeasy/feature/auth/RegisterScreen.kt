package com.example.farmeasy.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.farmeasy.ui.components.FormTextField
import com.example.farmeasy.ui.components.PrimaryButton
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onEvent: (AuthUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmer Registration", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = Spacing.large)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Create your farmer profile to receive tailored advisory.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormTextField(
                value = uiState.name,
                onValueChange = { onEvent(AuthUiEvent.NameChanged(it)) },
                label = "Full Name",
                placeholder = "e.g. Ramesh Patil",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(14.dp))

            FormTextField(
                value = uiState.phone,
                onValueChange = { onEvent(AuthUiEvent.PhoneChanged(it)) },
                label = "Mobile Number (10 digits)",
                placeholder = "e.g. 9876543210",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FormTextField(
                value = uiState.state,
                onValueChange = { onEvent(AuthUiEvent.StateChanged(it)) },
                label = "State",
                placeholder = "e.g. Maharashtra",
                leadingIcon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(14.dp))

            FormTextField(
                value = uiState.district,
                onValueChange = { onEvent(AuthUiEvent.DistrictChanged(it)) },
                label = "District",
                placeholder = "e.g. Pune",
                leadingIcon = Icons.Default.Place
            )

            Spacer(modifier = Modifier.height(14.dp))

            FormTextField(
                value = uiState.password,
                onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                label = "Choose Password (min 6 characters)",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Complete Registration",
                onClick = { onEvent(AuthUiEvent.RegisterSubmitted) },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
