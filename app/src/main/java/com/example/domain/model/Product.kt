package com.example.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val shopName: String = "NboaShop Officiel",
    val imageUrl: String = "",
    val isSponsored: Boolean = false,
    val boostBadge: String? = null,
    val category: String = "Général",
    val inStock: Boolean = true,
    val stockQuantity: Int = 10,
    val sellerId: String = "seller_official",
    val title: String = "",
    val currency: String = "XAF",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val displayTitle: String get() = if (title.isNotBlank()) title else name
}

@IgnoreExtraProperties
data class BankAccount(
    @DocumentId val id: String = "",
    val bankName: String = "",
    val accountName: String = "",
    val iban: String = "",
    val rib: String = "",
    val swift: String = "",
    val active: Boolean = true
)

data class Shop(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val itemsSold: String = "",
    val description: String = "",
    val avatarUrl: String = "",
    val isVerifiedPro: Boolean = false,
    val subscriptionTier: String = "STARTER"
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1
)

data class SellerStatus(
    val id: String = "",
    val shopName: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: String = "Récemment"
)

data class SellerAccount(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val pin: String = "1234",
    val timestamp: String = "Aujourd'hui",
    val subscriptionTier: String = "STARTER",
    val isVerified: Boolean = false,
    val remainingBoosts: Int = 0
)

data class DeliveryAccount(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val transport: String = "",
    val plateNumber: String = "",
    val location: String = "",
    val pin: String = "1234",
    val timestamp: String = "Aujourd'hui"
)

data class Review(
    val id: String = "",
    val targetType: String = "product", // "product" or "seller"
    val targetId: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val customerName: String = "Client NboaShop",
    val timestamp: Long = System.currentTimeMillis()
)
