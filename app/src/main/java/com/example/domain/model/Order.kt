package com.example.domain.model

data class Order(
    val id: String,
    val customerName: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val deliveryAddress: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OrderStatus(val label: String) {
    PENDING("En attente de paiement"),
    PAID("Payé - En préparation"),
    IN_TRANSIT("En cours de livraison"),
    DELIVERED("Livré"),
    CANCELLED("Annulé")
}
