package com.example.farmeasy.feature.marketplace

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.farmeasy.domain.model.CartItem
import com.example.farmeasy.ui.components.*
import com.example.farmeasy.ui.theme.DemoBannerBg
import com.example.farmeasy.ui.theme.DemoBannerText
import com.example.farmeasy.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: MarketplaceUiState,
    onEvent: (MarketplaceUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    if (uiState.lastPlacedOrder != null) {
        AlertDialog(
            onDismissRequest = { onEvent(MarketplaceUiEvent.DismissOrderConfirmation) },
            title = { Text("Order Placed Successfully!", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Order ID: ${uiState.lastPlacedOrder.id.take(8).uppercase()}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total: ₹${"%.2f".format(uiState.lastPlacedOrder.totalAmount)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Delivery To: ${uiState.lastPlacedOrder.deliveryAddress}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = DemoBannerBg,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "DEMO ORDER: Verified simulated order record saved in local database.",
                            color = DemoBannerText,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEvent(MarketplaceUiEvent.DismissOrderConfirmation)
                    onNavigateBack()
                }) {
                    Text("Done")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cartItems.isEmpty()) {
            EmptyStateView(
                title = "Your Cart is Empty",
                description = "Browse certified fertilizers, bio-pesticides, and tools in Kisan Marketplace.",
                actionText = "Shop Products",
                onAction = onNavigateBack,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.medium)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.cartItems, key = { it.product.id }) { item ->
                        CartListItem(
                            item = item,
                            onIncrement = { onEvent(MarketplaceUiEvent.UpdateCartQuantity(item.product.id, item.quantity + 1)) },
                            onDecrement = { onEvent(MarketplaceUiEvent.UpdateCartQuantity(item.product.id, item.quantity - 1)) },
                            onRemove = { onEvent(MarketplaceUiEvent.RemoveFromCart(item.product.id)) }
                        )
                    }
                }

                Surface(
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FormTextField(
                            value = uiState.deliveryAddressInput,
                            onValueChange = { onEvent(MarketplaceUiEvent.AddressChanged(it)) },
                            label = "Delivery Address",
                            leadingIcon = Icons.Default.LocationOn
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Payable Amount:", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "₹${"%.2f".format(uiState.cartTotal)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PrimaryButton(
                            text = "Confirm Order (Demo Checkout)",
                            onClick = { onEvent(MarketplaceUiEvent.PlaceOrder) },
                            isLoading = uiState.isCheckingOut,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartListItem(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    FarmEasyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${"%.2f".format(item.product.price)} each • Subtotal: ₹${"%.2f".format(item.subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
            }

            Text(
                text = "${item.quantity}",
                modifier = Modifier.padding(horizontal = 14.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            FilledTonalIconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}
