package com.example.farmeasy.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onLoggedOut: () -> Unit
) {
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLoggedOut()
        }
    }

    if (uiState.showLogoutConfirmation) {
        ConfirmationDialog(
            title = "Sign Out?",
            message = "Are you sure you want to log out from FarmEasy?",
            confirmText = "Log Out",
            onConfirm = { onEvent(ProfileUiEvent.LogoutConfirmed) },
            onDismiss = { onEvent(ProfileUiEvent.DismissLogoutDialog) }
        )
    }

    if (uiState.showBackendConfigDialog) {
        BackendConfigDialog(
            supabaseUrl = uiState.supabaseUrlInput,
            supabaseKey = uiState.supabaseAnonKeyInput,
            groqKey = uiState.groqApiKeyInput,
            onUrlChange = { onEvent(ProfileUiEvent.SupabaseUrlChanged(it)) },
            onKeyChange = { onEvent(ProfileUiEvent.SupabaseKeyChanged(it)) },
            onGroqChange = { onEvent(ProfileUiEvent.GroqKeyChanged(it)) },
            onSave = { onEvent(ProfileUiEvent.SaveBackendConfig) },
            onDismiss = { onEvent(ProfileUiEvent.DismissBackendDialog) }
        )
    }

    Scaffold(
        topBar = {
            FarmEasyTopBar(title = "Farmer Profile & Settings")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium)
        ) {
            // Profile Header Card
            FarmEasyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FarmerAvatar(name = uiState.farmer?.name ?: "Kisan", size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = uiState.farmer?.name ?: "Farmer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.farmer?.phone ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.farmer?.district ?: ""}, ${uiState.farmer?.state ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Preferences & Regional Languages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FarmEasyCard {
                Text(text = "Advisory Language", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("English", "Hindi", "Marathi").forEach { lang ->
                        FilterChip(
                            selected = uiState.preferences.selectedLanguage == lang,
                            onClick = { onEvent(ProfileUiEvent.LanguageChanged(lang)) },
                            label = { Text(lang) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "High-contrast nighttime view", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = uiState.preferences.isDarkMode,
                        onCheckedChange = { onEvent(ProfileUiEvent.DarkModeToggled(it)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Spoken Audio Readout", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = "Read assistant responses using Text-to-Speech", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = uiState.preferences.isVoiceEnabled,
                        onCheckedChange = { onEvent(ProfileUiEvent.VoiceToggled(it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cloud Integration & Developer Credentials",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FarmEasyCard {
                Text(
                    text = "Supabase PostgreSQL & Groq AI Config",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure live Supabase REST endpoints, Row-Level Security, or Groq API token for cloud synchronization.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onEvent(ProfileUiEvent.ShowBackendDialog) },
                    shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure Cloud API Services")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEvent(ProfileUiEvent.ShowLogoutDialog) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(Spacing.cornerRadiusMedium),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BackendConfigDialog(
    supabaseUrl: String,
    supabaseKey: String,
    groqKey: String,
    onUrlChange: (String) -> Unit,
    onKeyChange: (String) -> Unit,
    onGroqChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud & AI Configuration", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter optional Supabase and Groq keys to connect real production backends.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FormTextField(
                    value = supabaseUrl,
                    onValueChange = onUrlChange,
                    label = "Supabase URL",
                    placeholder = "https://your-project.supabase.co"
                )
                Spacer(modifier = Modifier.height(8.dp))
                FormTextField(
                    value = supabaseKey,
                    onValueChange = onKeyChange,
                    label = "Supabase Anon Key",
                    placeholder = "eyJhbGciOi..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                FormTextField(
                    value = groqKey,
                    onValueChange = onGroqChange,
                    label = "Groq API Key (Optional)",
                    placeholder = "gsk_..."
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
