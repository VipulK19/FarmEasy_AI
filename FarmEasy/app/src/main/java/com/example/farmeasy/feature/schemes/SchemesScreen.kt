package com.example.farmeasy.feature.schemes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.Scheme
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemesScreen(
    uiState: SchemeUiState,
    onEvent: (SchemeUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Govt Schemes & Subsidies", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = Spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            FarmSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(SchemeUiEvent.SearchQueryChanged(it)) },
                placeholder = "Search PM-KISAN, PMFBY, solar pump..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.selectedScheme != null) {
                SchemeDetailModal(
                    scheme = uiState.selectedScheme,
                    onDismiss = { onEvent(SchemeUiEvent.SchemeSelected(null)) },
                    onOpenUrl = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }

            if (uiState.schemes.isEmpty()) {
                EmptyStateView(
                    title = "No Schemes Found",
                    description = "No government subsidy matched '${uiState.searchQuery}'. Try another search query."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.schemes, key = { it.id }) { scheme ->
                        SchemeCard(
                            scheme = scheme,
                            onClick = { onEvent(SchemeUiEvent.SchemeSelected(scheme)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SchemeDetailModal(
    scheme: Scheme,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = scheme.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = scheme.ministry,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Benefits Overview:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = scheme.benefitSummary, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Eligibility Criteria:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = scheme.eligibility, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Required Documents:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                scheme.requiredDocuments.forEach { doc ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = doc, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "How to Apply:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = scheme.applicationSteps, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Last verified with official portal: ${scheme.lastVerifiedDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onOpenUrl(scheme.officialUrl) },
                shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Visit Official Portal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
