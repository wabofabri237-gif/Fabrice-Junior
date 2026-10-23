package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreProvider
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrderRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreProvider.get(context))
    constructor() : this(FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext))

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore orders.")
    }

    private fun mapDocumentToOrder(doc: DocumentSnapshot): Order {
        val id = doc.id
        val customerName = doc.getString("customerName") ?: "Client NboaShop"
        val statusStr = doc.getString("status") ?: "PENDING"
        val status = OrderStatus.fromString(statusStr)
        val totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0
        val deliveryAddress = doc.getString("deliveryAddress") ?: ""
        val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        val paymentProvider = doc.getString("paymentProvider")
        val transactionRef = doc.getString("transactionRef")
        val payerPhone = doc.getString("payerPhone")
        val country = doc.getString("country")
        val userId = doc.getString("userId") ?: ""
        val sellerId = doc.getString("sellerId") ?: ""
        val courierId = doc.getString("courierId") ?: ""
        val paymentMethod = doc.getString("paymentMethod") ?: "CASH_ON_DELIVERY"
        val deliveryCity = doc.getString("deliveryCity") ?: "Douala"
        val contactPhone = doc.getString("contactPhone") ?: ""
        val deliveryOtp = doc.getString("deliveryOtp") ?: ""
        val bankTransferRef = doc.getString("bankTransferRef") ?: ""
        val bankAccountName = doc.getString("bankAccountName") ?: ""
        val itemsSummary = doc.getString("itemsSummary") ?: ""
        val createdAt = doc.getTimestamp("createdAt")
        val updatedAt = doc.getTimestamp("updatedAt")

        return Order(
            id = id,
            customerName = customerName,
            status = status,
            totalAmount = totalAmount,
            deliveryAddress = deliveryAddress,
            timestamp = timestamp,
            paymentProvider = paymentProvider,
            transactionRef = transactionRef,
            payerPhone = payerPhone,
            country = country,
            userId = userId,
            sellerId = sellerId,
            courierId = courierId,
            paymentMethod = paymentMethod,
            deliveryCity = deliveryCity,
            contactPhone = contactPhone,
            deliveryOtp = deliveryOtp,
            bankTransferRef = bankTransferRef,
            bankAccountName = bankAccountName,
            itemsSummary = itemsSummary,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun observeUserOrders(userId: String = auth.currentUser?.uid ?: ""): Flow<List<Order>> {
        val uid = if (userId.isNotBlank()) userId else (auth.currentUser?.uid ?: "")
        if (uid.isBlank()) return flow { emit(emptyList()) }
        
        return db.collection("orders")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { mapDocumentToOrder(it) }.sortedByDescending { it.timestamp }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "orders?userId=$uid")
                emit(emptyList())
            }
    }

    fun observeSellerOrders(sellerId: String = auth.currentUser?.uid ?: ""): Flow<List<Order>> {
        val sId = if (sellerId.isNotBlank()) sellerId else (auth.currentUser?.uid ?: "")
        if (sId.isBlank()) return flow { emit(emptyList()) }

        return db.collection("orders")
            .whereEqualTo("sellerId", sId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { mapDocumentToOrder(it) }.sortedByDescending { it.timestamp }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "orders?sellerId=$sId")
                emit(emptyList())
            }
    }

    fun observeCourierOrders(courierId: String = auth.currentUser?.uid ?: ""): Flow<List<Order>> {
        val cId = if (courierId.isNotBlank()) courierId else (auth.currentUser?.uid ?: "")
        if (cId.isBlank()) return flow { emit(emptyList()) }

        return db.collection("orders")
            .whereEqualTo("courierId", cId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { mapDocumentToOrder(it) }.sortedByDescending { it.timestamp }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "orders?courierId=$cId")
                emit(emptyList())
            }
    }

    fun getActiveOrdersStream(): Flow<List<Order>> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            observeUserOrders(uid)
        } else {
            flow { emit(emptyList()) }
        }
    }

    suspend fun createOrder(
        customerName: String,
        sellerId: String,
        totalAmount: Double,
        paymentMethod: String, // CASH_ON_DELIVERY or BANK_TRANSFER
        deliveryAddress: String,
        deliveryCity: String,
        contactPhone: String,
        itemsSummary: String,
        bankAccountName: String = "",
        bankTransferRef: String = ""
    ): Result<Order> {
        val uid = requireUserId()
        val orderId = "order_" + UUID.randomUUID().toString().take(12)
        val generatedOtp = (1000..9999).random().toString()

        val data = hashMapOf<String, Any>(
            "id" to orderId,
            "userId" to uid,
            "customerName" to customerName,
            "sellerId" to if (sellerId.isNotBlank()) sellerId else "seller_official",
            "courierId" to "",
            "totalAmount" to totalAmount.toLong(),
            "paymentMethod" to paymentMethod,
            "deliveryAddress" to deliveryAddress,
            "deliveryCity" to deliveryCity,
            "contactPhone" to contactPhone,
            "deliveryOtp" to generatedOtp,
            "bankAccountName" to bankAccountName,
            "bankTransferRef" to bankTransferRef,
            "itemsSummary" to itemsSummary,
            "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            db.collection("orders").document(orderId).set(data).await()
            Result.success(
                Order(
                    id = orderId,
                    userId = uid,
                    customerName = customerName,
                    sellerId = sellerId,
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    deliveryAddress = deliveryAddress,
                    deliveryCity = deliveryCity,
                    contactPhone = contactPhone,
                    deliveryOtp = generatedOtp,
                    bankAccountName = bankAccountName,
                    bankTransferRef = bankTransferRef,
                    itemsSummary = itemsSummary,
                    status = OrderStatus.PENDING
                )
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "orders/$orderId")
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus, courierId: String? = null): Boolean {
        return try {
            val updatePayload = mutableMapOf<String, Any>(
                "status" to newStatus.code,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (courierId != null) {
                updatePayload["courierId"] = courierId
            }
            db.collection("orders").document(orderId).update(updatePayload).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "orders/$orderId")
            false
        }
    }

    suspend fun verifyAndCompleteDelivery(orderId: String, otpInput: String): Result<Boolean> {
        return try {
            val snapshot = db.collection("orders").document(orderId).get().await()
            val expectedOtp = snapshot.getString("deliveryOtp") ?: ""
            if (expectedOtp.trim() == otpInput.trim() && expectedOtp.isNotBlank()) {
                db.collection("orders").document(orderId).update(
                    mapOf(
                        "status" to "DELIVERED",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "orders/$orderId")
            Result.failure(e)
        }
    }
}
