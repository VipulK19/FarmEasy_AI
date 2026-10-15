package com.example.farmeasy.feature.dealer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.Dealer
import com.example.farmeasy.ui.components.EmptyStateView
import com.example.farmeasy.ui.components.FarmEasyCard
import com.example.farmeasy.ui.components.FarmSearchBar
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerLocatorScreen(
    uiState: DealerUiState,
    onEvent: (DealerUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agri Dealer Locator", fontWeight = FontWeight.Bold) },
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
                onQueryChange = { onEvent(DealerUiEvent.SearchQueryChanged(it)) },
                placeholder = "Search fertilizers, seeds, implements, agency..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.dealers.isEmpty()) {
                EmptyStateView(
                    title = "No Dealers Found",
                    description = "No agricultural suppliers matched '${uiState.searchQuery}'. Try another category or region."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.dealers, key = { it.id }) { dealer ->
                        DealerCard(
                            dealer = dealer,
                            onCall = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            },
                            onOpenMap = { lat, lng, name ->
                                val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($name)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(mapIntent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DealerCard(
    dealer: Dealer,
    onCall: (String) -> Unit,
    onOpenMap: (Double, Double, String) -> Unit
) {
    FarmEasyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dealer.businessName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${dealer.distanceKm} km away",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = dealer.dealerType,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = dealer.address,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onCall(dealer.phone) },
                shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onOpenMap(dealer.latitude, dealer.longitude, dealer.businessName) },
                shape = RoundedCornerShape(Spacing.cornerRadiusSmall)
            ) {
                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Map Directions")
            }
        }
    }
}
