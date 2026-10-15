package com.example.farmeasy.feature.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmeasy.domain.model.CartItem
import com.example.farmeasy.domain.model.MarketplaceProduct
import com.example.farmeasy.domain.model.Order
import com.example.farmeasy.domain.usecase.ManageMarketplaceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarketplaceUiState(
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val products: List<MarketplaceProduct> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val deliveryAddressInput: String = "Farm Parcel 1, Haveli, Pune",
    val isCheckingOut: Boolean = false,
    val lastPlacedOrder: Order? = null,
    val errorMessage: String? = null
) {
    val cartTotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

sealed interface MarketplaceUiEvent {
    data class CategorySelected(val category: String) : MarketplaceUiEvent
    data class SearchQueryChanged(val query: String) : MarketplaceUiEvent
    data class AddToCart(val product: MarketplaceProduct) : MarketplaceUiEvent
    data class UpdateCartQuantity(val productId: String, val newQuantity: Int) : MarketplaceUiEvent
    data class RemoveFromCart(val productId: String) : MarketplaceUiEvent
    data class AddressChanged(val address: String) : MarketplaceUiEvent
    object PlaceOrder : MarketplaceUiEvent
    object DismissOrderConfirmation : MarketplaceUiEvent
    object DismissError : MarketplaceUiEvent
}

class MarketplaceViewModel(
    private val useCase: ManageMarketplaceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()

    private var allProducts = listOf<MarketplaceProduct>()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            useCase.getProducts().collect { list ->
                allProducts = list
                filterProducts(_uiState.value.selectedCategory, _uiState.value.searchQuery)
            }
        }

        viewModelScope.launch {
            useCase.getCart().collect { cart ->
                _uiState.update { it.copy(cartItems = cart) }
            }
        }

        viewModelScope.launch {
            useCase.getOrders().collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }

    fun onEvent(event: MarketplaceUiEvent) {
        when (event) {
            is MarketplaceUiEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                filterProducts(event.category, _uiState.value.searchQuery)
            }
            is MarketplaceUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterProducts(_uiState.value.selectedCategory, event.query)
            }
            is MarketplaceUiEvent.AddToCart -> {
                viewModelScope.launch { useCase.addToCart(event.product) }
            }
            is MarketplaceUiEvent.UpdateCartQuantity -> {
                viewModelScope.launch { useCase.updateQuantity(event.productId, event.newQuantity) }
            }
            is MarketplaceUiEvent.RemoveFromCart -> {
                viewModelScope.launch { useCase.removeFromCart(event.productId) }
            }
            is MarketplaceUiEvent.AddressChanged -> _uiState.update { it.copy(deliveryAddressInput = event.address) }
            is MarketplaceUiEvent.PlaceOrder -> placeOrder()
            is MarketplaceUiEvent.DismissOrderConfirmation -> _uiState.update { it.copy(lastPlacedOrder = null) }
            is MarketplaceUiEvent.DismissError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun filterProducts(category: String, query: String) {
        var list = allProducts
        if (category != "All") {
            list = list.filter { it.category == category }
        }
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.brand.lowercase().contains(q) ||
                        it.description.lowercase().contains(q)
            }
        }
        _uiState.update { it.copy(products = list) }
    }

    private fun placeOrder() {
        val s = _uiState.value
        _uiState.update { it.copy(isCheckingOut = true, errorMessage = null) }
        viewModelScope.launch {
            val result = useCase.checkout(s.deliveryAddressInput)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isCheckingOut = false,
                        lastPlacedOrder = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isCheckingOut = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Order checkout failed."
                    )
                }
            }
        }
    }
}
