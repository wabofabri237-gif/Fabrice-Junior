package com.example.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentApi {
    
    @POST("v1/orange-money/pay")
    suspend fun initiateOrangeMoneyPayment(@Body request: PaymentRequest): PaymentResponse

    @POST("v1/mtn-momo/pay")
    suspend fun initiateMtnMomoPayment(@Body request: PaymentRequest): PaymentResponse
}

data class PaymentRequest(
    val amount: Double,
    val currency: String = "XAF",
    val customerPhone: String,
    val orderId: String
)

data class PaymentResponse(
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val transactionId: String,
    val message: String
)
