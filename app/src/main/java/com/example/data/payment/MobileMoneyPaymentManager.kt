package com.example.data.payment

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class PaymentOperator(
    val displayName: String,
    val code: String,
    val brandColorHex: Long,
    val apiType: String,
    val logoText: String
) {
    ORANGE_MONEY(
        displayName = "Orange Money",
        code = "OM",
        brandColorHex = 0xFFFF7900,
        apiType = "Orange Money WebPay / USSD Push MEA API",
        logoText = "🟠 Orange Money"
    ),
    MTN_MOMO(
        displayName = "MTN Mobile Money",
        code = "MOMO",
        brandColorHex = 0xFFFFCC00,
        apiType = "MTN MoMo Open API Collections v1.0",
        logoText = "🟡 MTN MoMo"
    )
}

enum class CentralAfricanCountry(
    val countryName: String,
    val dialCode: String,
    val flagEmoji: String,
    val currency: String = "XAF",
    val defaultOrangeUssd: String,
    val defaultMtnUssd: String,
    val exampleNumber: String
) {
    CAMEROON("Cameroun", "+237", "🇨🇲", "XAF", "#150*50#", "*126#", "690123456 / 670123456"),
    GABON("Gabon", "+241", "🇬🇦", "XAF", "*144#", "*555#", "074123456 / 066123456"),
    CONGO("Congo (Brazzaville)", "+242", "🇨🇬", "XAF", "*144#", "*105#", "066123456 / 055123456"),
    CHAD("Tchad", "+235", "🇹🇩", "XAF", "*144#", "*150#", "66123456 / 99123456"),
    CAR("Centrafrique", "+236", "🇨🇫", "XAF", "*144#", "*100#", "75123456 / 70123456"),
    EQUATORIAL_GUINEA("Guinée Équatoriale", "+240", "XAF", "🇬🇶", "*144#", "*123#", "222123456 / 555123456");

    fun getUssdForOperator(operator: PaymentOperator): String {
        return when (operator) {
            PaymentOperator.ORANGE_MONEY -> defaultOrangeUssd
            PaymentOperator.MTN_MOMO -> defaultMtnUssd
        }
    }
}

enum class PaymentTransactionStatus(val label: String) {
    INITIATED("Initialisation sécurisée"),
    PENDING_USSD_PUSH("En attente de validation PIN sur le téléphone"),
    PROCESSING("Traitement bancaire CEMAC"),
    SUCCESSFUL("Paiement Confirmé & Enregistré"),
    FAILED("Échec de la transaction")
}

data class PaymentTransaction(
    val transactionId: String,
    val orderId: String,
    val amount: Double,
    val currency: String = "XAF",
    val operator: PaymentOperator,
    val country: CentralAfricanCountry,
    val phoneNumber: String,
    val customerName: String,
    val status: PaymentTransactionStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val ussdInstruction: String,
    val message: String
)

object MobileMoneyPaymentManager {

    private const val TAG = "MobileMoneyPayment"

    /**
     * Executes the secure end-to-end payment processing sequence:
     * 1. Creates transaction record
     * 2. Simulates operator API handshake & USSD prompt
     * 3. Syncs transaction to Firestore ('payment_transactions' collection)
     * 4. Updates order status in Firestore ('orders' collection) to PAID
     */
    suspend fun processPayment(
        orderId: String,
        amount: Double,
        operator: PaymentOperator,
        country: CentralAfricanCountry,
        phoneNumber: String,
        customerName: String,
        deliveryAddress: String,
        onStatusUpdate: (PaymentTransactionStatus, String) -> Unit
    ): Result<PaymentTransaction> {
        val uniqueSuffix = (System.currentTimeMillis() % 1000000).toString().padStart(6, '0')
        val transactionRef = "TXN-${operator.code}-${country.dialCode.replace("+", "")}-$uniqueSuffix"
        val ussdCode = country.getUssdForOperator(operator)

        try {
            // Step 1: Initiated
            onStatusUpdate(
                PaymentTransactionStatus.INITIATED,
                "Connexion à la passerelle ${operator.displayName} (${country.flagEmoji} ${country.countryName})..."
            )
            delay(800)

            // Step 2: USSD Push Sent
            onStatusUpdate(
                PaymentTransactionStatus.PENDING_USSD_PUSH,
                "Push USSD envoyé vers $phoneNumber. Veuillez taper votre code secret sur votre téléphone ou composer $ussdCode"
            )
            delay(1500)

            // Step 3: Bank Processing
            onStatusUpdate(
                PaymentTransactionStatus.PROCESSING,
                "Validation du débit de ${"%.0f".format(amount)} XAF auprès de la BEAC / ${operator.displayName}..."
            )
            delay(1200)

            val successfulTransaction = PaymentTransaction(
                transactionId = transactionRef,
                orderId = orderId,
                amount = amount,
                currency = "XAF",
                operator = operator,
                country = country,
                phoneNumber = phoneNumber,
                customerName = customerName,
                status = PaymentTransactionStatus.SUCCESSFUL,
                timestamp = System.currentTimeMillis(),
                ussdInstruction = ussdCode,
                message = "Paiement de ${"%.0f".format(amount)} XAF validé avec succès via ${operator.displayName}."
            )

            // Step 4: Sync to Firestore
            syncTransactionAndOrderToFirestore(
                transaction = successfulTransaction,
                deliveryAddress = deliveryAddress
            )

            onStatusUpdate(
                PaymentTransactionStatus.SUCCESSFUL,
                "Paiement validé ! Référence : $transactionRef"
            )

            return Result.success(successfulTransaction)

        } catch (e: Exception) {
            Log.e(TAG, "Payment failed for order $orderId", e)
            val failedTransaction = PaymentTransaction(
                transactionId = transactionRef,
                orderId = orderId,
                amount = amount,
                currency = "XAF",
                operator = operator,
                country = country,
                phoneNumber = phoneNumber,
                customerName = customerName,
                status = PaymentTransactionStatus.FAILED,
                timestamp = System.currentTimeMillis(),
                ussdInstruction = ussdCode,
                message = "Échec du paiement : ${e.localizedMessage ?: "Erreur réseau opérateur"}"
            )
            onStatusUpdate(PaymentTransactionStatus.FAILED, failedTransaction.message)
            return Result.failure(e)
        }
    }

    /**
     * Persists payment record and updates order status to PAID in Firestore.
     */
    suspend fun syncTransactionAndOrderToFirestore(
        transaction: PaymentTransaction,
        deliveryAddress: String
    ) {
        try {
            val db = com.example.data.firebase.FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext)

            // 1. Write transaction to 'payment_transactions' collection
            val transactionData = hashMapOf(
                "transactionId" to transaction.transactionId,
                "orderId" to transaction.orderId,
                "amount" to transaction.amount,
                "currency" to transaction.currency,
                "operator" to transaction.operator.name,
                "operatorCode" to transaction.operator.code,
                "country" to transaction.country.countryName,
                "countryCode" to transaction.country.dialCode,
                "phoneNumber" to transaction.phoneNumber,
                "customerName" to transaction.customerName,
                "status" to transaction.status.name,
                "timestamp" to transaction.timestamp,
                "apiGateway" to transaction.operator.apiType
            )
            db.collection("payment_transactions")
                .document(transaction.transactionId)
                .set(transactionData, SetOptions.merge())
                .await()

            // 2. Update order in 'orders' collection to PAID
            val orderUpdate = hashMapOf(
                "id" to transaction.orderId,
                "customerName" to transaction.customerName,
                "status" to OrderStatus.PAID.name,
                "statusLabel" to OrderStatus.PAID.label,
                "totalAmount" to transaction.amount,
                "deliveryAddress" to deliveryAddress,
                "paymentProvider" to transaction.operator.displayName,
                "paymentTransactionId" to transaction.transactionId,
                "payerPhone" to transaction.phoneNumber,
                "country" to "${transaction.country.flagEmoji} ${transaction.country.countryName}",
                "lastUpdated" to System.currentTimeMillis()
            )
            db.collection("orders")
                .document(transaction.orderId)
                .set(orderUpdate, SetOptions.merge())
                .await()

            // 3. Write an audit log entry in 'order_audit_logs'
            val auditLog = hashMapOf(
                "orderId" to transaction.orderId,
                "eventType" to "PAYMENT_SUCCESS",
                "transactionId" to transaction.transactionId,
                "provider" to transaction.operator.code,
                "amount" to transaction.amount,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("orders")
                .document(transaction.orderId)
                .collection("audit_logs")
                .document(transaction.transactionId)
                .set(auditLog, SetOptions.merge())
                .await()

            Log.d(TAG, "Firestore successfully updated for transaction: ${transaction.transactionId}")

        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync failed (offline or uninitialized): ${e.message}")
        }
    }

    /**
     * Checks if a phone number matches Orange Money or MTN prefixes in Central Africa
     */
    fun detectSuggestedOperator(phoneNumber: String, country: CentralAfricanCountry): PaymentOperator? {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        return when (country) {
            CentralAfricanCountry.CAMEROON -> {
                when {
                    cleanNumber.startsWith("69") || cleanNumber.startsWith("655") ||
                    cleanNumber.startsWith("656") || cleanNumber.startsWith("657") ||
                    cleanNumber.startsWith("658") || cleanNumber.startsWith("659") -> PaymentOperator.ORANGE_MONEY

                    cleanNumber.startsWith("67") || cleanNumber.startsWith("68") ||
                    cleanNumber.startsWith("650") || cleanNumber.startsWith("651") ||
                    cleanNumber.startsWith("652") || cleanNumber.startsWith("653") ||
                    cleanNumber.startsWith("654") -> PaymentOperator.MTN_MOMO

                    else -> null
                }
            }
            CentralAfricanCountry.GABON -> {
                when {
                    cleanNumber.startsWith("074") || cleanNumber.startsWith("077") -> PaymentOperator.ORANGE_MONEY
                    cleanNumber.startsWith("066") || cleanNumber.startsWith("062") -> PaymentOperator.MTN_MOMO
                    else -> null
                }
            }
            CentralAfricanCountry.CONGO -> {
                when {
                    cleanNumber.startsWith("06") -> PaymentOperator.MTN_MOMO
                    cleanNumber.startsWith("05") || cleanNumber.startsWith("04") -> PaymentOperator.ORANGE_MONEY
                    else -> null
                }
            }
            else -> null
        }
    }
}
