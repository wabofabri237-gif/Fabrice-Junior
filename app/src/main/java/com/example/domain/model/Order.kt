package com.example.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val customerName: String = "Client NboaShop",
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double = 0.0,
    val deliveryAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val paymentProvider: String? = null,
    val transactionRef: String? = null,
    val payerPhone: String? = null,
    val country: String? = null,
    val userId: String = "",
    val sellerId: String = "seller_official",
    val courierId: String = "",
    val paymentMethod: String = "CASH_ON_DELIVERY", // CASH_ON_DELIVERY or BANK_TRANSFER
    val deliveryCity: String = "Douala",
    val contactPhone: String = "",
    val deliveryOtp: String = "",
    val bankTransferRef: String = "",
    val bankAccountName: String = "",
    val itemsSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class OrderStatus(val label: String) {
    PENDING("En attente de confirmation"),
    PAID("Payé - En préparation"),
    PROCESSING("En préparation"),
    CONFIRMED("Confirmée"),
    IN_TRANSIT("En cours de livraison"),
    DELIVERED("Livré"),
    CANCELLED("Annulé");

    val code: String get() = name

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}
