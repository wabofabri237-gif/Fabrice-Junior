package com.example.domain.model

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val shopName: String,
    val imageUrl: String = ""
)

data class Shop(
    val id: String = "",
    val name: String,
    val location: String,
    val itemsSold: String,
    val description: String,
    val avatarUrl: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1
)

data class SellerStatus(
    val id: String,
    val shopName: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: String = "Récemment"
)

data class SellerAccount(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val pin: String = "1234",
    val timestamp: String = "Aujourd'hui"
)

data class DeliveryAccount(
    val id: String,
    val name: String,
    val phone: String,
    val transport: String,
    val plateNumber: String,
    val location: String,
    val pin: String = "1234",
    val timestamp: String = "Aujourd'hui"
)

