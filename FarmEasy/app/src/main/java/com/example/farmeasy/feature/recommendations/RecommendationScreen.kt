package com.example.farmeasy.feature.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.ui.components.EmptyStateView
import com.example.farmeasy.ui.components.RecommendationCard
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    uiState: RecommendationUiState,
    onEvent: (RecommendationUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val categories = listOf("All", "Fertilizer", "Disease", "Irrigation", "Weather")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recommendations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(RecommendationUiEvent.Refresh) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = { onEvent(RecommendationUiEvent.CategorySelected(cat)) },
                        label = { Text(cat) }
                    )
                }
            }

            if (uiState.recommendations.isEmpty()) {
                EmptyStateView(
                    title = "No recommendations in this category",
                    description = "Change filter or refresh to view latest agronomic guidelines."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.recommendations, key = { it.id }) { rec ->
                        RecommendationCard(recommendation = rec)
                    }
                }
            }
        }
    }
}
