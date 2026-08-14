package com.example.data.repository

import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OrderRepository {

    // Simulates a real-time Firestore stream
    fun getActiveOrdersStream(): Flow<List<Order>> = flow {
        // Initial state
        val orders = mutableListOf(
            Order("CMD-49201", "Jean Dupont", OrderStatus.IN_TRANSIT, 24500.0, "Quartier Bonanjo, Douala"),
            Order("CMD-49202", "Alice Manga", OrderStatus.PENDING, 12000.0, "Akwa, Douala")
        )
        emit(orders.toList())

        // Simulate real-time updates every few seconds
        delay(5000)
        
        // Update first order to DELIVERED
        val updatedOrders = orders.map { 
            if (it.id == "CMD-49201") it.copy(status = OrderStatus.DELIVERED) else it
        }.toMutableList()
        
        // Add a new order
        updatedOrders.add(
            Order("CMD-49203", "Paul Kamga", OrderStatus.PAID, 45000.0, "Bastos, Yaoundé")
        )
        
        emit(updatedOrders.toList())
    }

    // Example function to update order status (would call Firestore API in production)
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Boolean {
        delay(1000) // Network latency simulation
        return true
    }
}
