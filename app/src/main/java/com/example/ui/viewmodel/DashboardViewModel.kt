package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.domain.model.CartItem
import com.example.domain.model.DeliveryAccount
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.example.domain.model.Product
import com.example.domain.model.SellerAccount
import com.example.domain.model.SellerStatus
import com.example.domain.model.Shop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _sellerAccounts = MutableStateFlow<List<SellerAccount>>(
        listOf(SellerAccount("s_default", "La Boutique de Marie", "Boutique officielle", "Douala", "1234"))
    )
    val sellerAccounts: StateFlow<List<SellerAccount>> = _sellerAccounts.asStateFlow()

    private val _deliveryAccounts = MutableStateFlow<List<DeliveryAccount>>(
        listOf(DeliveryAccount("d_default", "Jean Dupont", "+237 600000000", "Moto", "LT-123-AB", "Douala", "1234"))
    )
    val deliveryAccounts: StateFlow<List<DeliveryAccount>> = _deliveryAccounts.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _statuses = MutableStateFlow<List<SellerStatus>>(
        listOf(
            SellerStatus("s1", "La Boutique de Marie", "https://images.unsplash.com/photo-1548036328-c9fa89d128fa", "Nouveaux sacs artisanaux disponibles !"),
            SellerStatus("s2", "Cuir d'Afrique", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2", "Collection chaussures cuir pur."),
            SellerStatus("s3", "Mode Kmer", "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b", "Robes traditionnelles et modernes.")
        )
    )
    val statuses: StateFlow<List<SellerStatus>> = _statuses.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tous")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _currentShop = MutableStateFlow<Shop?>(
        Shop(
            id = "default_shop",
            name = "La Boutique de Marie",
            location = "Marché Central, Douala",
            itemsSold = "Artisanat, Vêtements, Accessoires",
            description = "Boutique officielle proposant les meilleurs articles du marché."
        )
    )
    val currentShop: StateFlow<Shop?> = _currentShop.asStateFlow()

    private val _connectedSocials = MutableStateFlow<Set<String>>(emptySet())
    val connectedSocials: StateFlow<Set<String>> = _connectedSocials.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchActiveOrders()
        loadMockProducts()
    }

    private fun loadMockProducts() {
        _products.value = listOf(
            Product("p1", "Sac à main artisanal", "Fait main avec des matériaux locaux de qualité supérieure.", 15000.0, "La Boutique de Marie", "https://images.unsplash.com/photo-1548036328-c9fa89d128fa"),
            Product("p2", "Chaussures en cuir", "Confortables et durables pour toutes vos occasions.", 25000.0, "Cuir d'Afrique", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2"),
            Product("p3", "Robe en pagne", "Design moderne et traditionnel élégant.", 18000.0, "Mode Kmer", "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b"),
            Product("p4", "Écouteurs sans fil", "Son haute fidélité et autonomie longue durée.", 12000.0, "Tech Market", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"),
            Product("p5", "Crème cosmétique bio", "Soin naturel pour éclat de la peau.", 8000.0, "Cosmétiques Douceur", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e")
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun createShop(name: String, location: String, itemsSold: String, description: String) {
        _currentShop.value = Shop(
            id = "shop_${System.currentTimeMillis()}",
            name = name,
            location = location,
            itemsSold = itemsSold,
            description = description
        )
    }

    fun addProduct(name: String, description: String, price: Double, imageUrl: String) {
        val shopName = _currentShop.value?.name ?: "Ma Boutique"
        val newProduct = Product(
            id = "prod_${System.currentTimeMillis()}",
            name = name,
            description = description,
            price = price,
            shopName = shopName,
            imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1523275335684-37898b6baf30" }
        )
        _products.value = listOf(newProduct) + _products.value
    }

    fun postStatus(imageUrl: String, caption: String) {
        val shopName = _currentShop.value?.name ?: "Ma Boutique"
        val newStatus = SellerStatus(
            id = "status_${System.currentTimeMillis()}",
            shopName = shopName,
            imageUrl = imageUrl,
            caption = caption,
            timestamp = "À l'instant"
        )
        _statuses.value = listOf(newStatus) + _statuses.value
    }

    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val item = currentCart[existingIndex]
            currentCart[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            currentCart.add(CartItem(product))
        }
        _cart.value = currentCart
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun createOrder(customerName: String, totalAmount: Double, deliveryAddress: String): Order {
        val orderId = "CMD-${System.currentTimeMillis() % 100000}"
        val purchasedItems = _cart.value.map {
            mapOf(
                "productName" to it.product.name,
                "price" to it.product.price,
                "quantity" to it.quantity
            )
        }

        val newOrder = Order(
            id = orderId,
            customerName = customerName,
            status = OrderStatus.PAID,
            totalAmount = totalAmount,
            deliveryAddress = deliveryAddress
        )
        
        // Save to Firestore safely
        try {
            val db = FirebaseFirestore.getInstance()
            val orderMap = hashMapOf(
                "id" to orderId,
                "customerName" to customerName,
                "status" to OrderStatus.PAID.name,
                "totalAmount" to totalAmount,
                "deliveryAddress" to deliveryAddress,
                "timestamp" to System.currentTimeMillis(),
                "items" to purchasedItems
            )
            db.collection("orders").document(orderId).set(orderMap)
        } catch (e: Exception) {
            // Fallback local only if Firebase not initialized
        }

        _orders.value = listOf(newOrder) + _orders.value
        _cart.value = emptyList()
        return newOrder
    }

    fun registerSeller(name: String, description: String, location: String, pin: String) {
        val newAccount = SellerAccount(
            id = "seller_${System.currentTimeMillis()}",
            name = name,
            description = description,
            location = location,
            pin = pin.ifEmpty { "1234" }
        )
        _sellerAccounts.value = listOf(newAccount) + _sellerAccounts.value
        createShop(name, location, "Divers", description)
    }

    fun registerDelivery(name: String, phone: String, transport: String, plateNumber: String, location: String, pin: String) {
        val newAccount = DeliveryAccount(
            id = "delivery_${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            transport = transport,
            plateNumber = plateNumber,
            location = location,
            pin = pin.ifEmpty { "1234" }
        )
        _deliveryAccounts.value = listOf(newAccount) + _deliveryAccounts.value
    }

    fun authenticateSeller(sellerId: String, pin: String): Boolean {
        val account = _sellerAccounts.value.find { it.id == sellerId }
        if (account != null && account.pin == pin) {
            // Set current shop to this seller's shop
            createShop(account.name, account.location, "Divers", account.description)
            return true
        }
        return false
    }

    fun authenticateDelivery(deliveryId: String, pin: String): Boolean {
        val account = _deliveryAccounts.value.find { it.id == deliveryId }
        return account != null && account.pin == pin
    }

    fun toggleSocialConnection(platform: String) {
        val current = _connectedSocials.value.toMutableSet()
        if (current.contains(platform)) {
            current.remove(platform)
        } else {
            current.add(platform)
        }
        _connectedSocials.value = current
    }

    private fun fetchActiveOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getActiveOrdersStream()
                .catch { _isLoading.value = false }
                .collect { orderList ->
                    _orders.value = orderList
                    _isLoading.value = false
                }
        }
    }
}
