package com.example.data.monetization

import android.util.Log
import com.example.data.payment.CentralAfricanCountry
import com.example.data.payment.PaymentOperator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceXaf: Double,
    val durationDays: Int,
    val commissionRatePercent: Double,
    val maxProducts: Int,
    val freeBoosts: Int,
    val badgeLabel: String,
    val perks: List<String>
) {
    FREE(
        id = "plan_free",
        title = "Starter Gratuit",
        priceXaf = 0.0,
        durationDays = 3650,
        commissionRatePercent = 5.0,
        maxProducts = 10,
        freeBoosts = 0,
        badgeLabel = "Marchand Standard",
        perks = listOf("Jusqu'à 10 articles", "Commission plateforme 5%", "Support standard")
    ),
    PRO(
        id = "plan_pro",
        title = "Vendeur Pro (Gold)",
        priceXaf = 4900.0,
        durationDays = 30,
        commissionRatePercent = 2.0,
        maxProducts = 50,
        freeBoosts = 2,
        badgeLabel = "✨ Marchand Vérifié Pro",
        perks = listOf("Jusqu'à 50 articles", "Commission réduite à 2%", "2 Boosts produits offerts", "Badge Vérifié Pro ✨", "Statistiques de ventes avancées")
    ),
    VIP(
        id = "plan_vip",
        title = "Vendeur VIP Diamant",
        priceXaf = 12500.0,
        durationDays = 30,
        commissionRatePercent = 0.0,
        maxProducts = 9999,
        freeBoosts = 5,
        badgeLabel = "💎 Marchand VIP Partenaire",
        perks = listOf("Articles illimités", "0% de commission sur les ventes", "5 Boosts produits offerts", "Badge VIP Diamant 💎", "Bannière publicitaire prioritaire", "Support dédié 24/7")
    )
}

enum class BoostPackage(
    val id: String,
    val title: String,
    val priceXaf: Double,
    val durationDays: Int,
    val description: String
) {
    BOOST_3_DAYS("boost_3d", "Boost Flash 3 Jours", 750.0, 3, "Propulse l'article en tête de liste pendant 72h"),
    BOOST_7_DAYS("boost_7d", "Super Boost 7 Jours", 1500.0, 7, "Mise en avant premium dans le carrousel vedette pendant 1 semaine"),
    BOOST_14_DAYS("boost_14d", "Mega Boost VIP 14 Jours", 2800.0, 14, "Position n°1 garantie, badge '⭐ Sponsorisé' et visibilité maximale 2 semaines")
}

data class PlatformConfig(
    val standardCommissionRate: Double = 5.0, // 5%
    val platformBuyerServiceFee: Double = 200.0, // 200 XAF par commande
    val deliveryPartnerSplitPercent: Double = 85.0 // 85% pour le livreur, 15% pour la plateforme
)

data class RevenueEntry(
    val id: String = UUID.randomUUID().toString(),
    val source: String, // "COMMISSION", "SUBSCRIPTION", "BOOST", "SERVICE_FEE"
    val amount: Double,
    val description: String,
    val referenceId: String,
    val paymentOperator: String = "Orange Money",
    val timestamp: Long = System.currentTimeMillis()
)

data class SellerSubscription(
    val sellerId: String,
    val sellerName: String,
    val tier: SubscriptionTier,
    val startDate: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val active: Boolean = true,
    val paymentTxnId: String = ""
)

data class BoostedProductRecord(
    val productId: String,
    val productName: String,
    val shopName: String,
    val boostPackage: BoostPackage,
    val startDate: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val active: Boolean = true,
    val paymentTxnId: String = ""
)

data class PayoutRequest(
    val id: String = "PAYOUT-${System.currentTimeMillis() % 1000000}",
    val amount: Double,
    val recipientPhone: String,
    val operator: PaymentOperator,
    val country: CentralAfricanCountry,
    val status: String = "EFFECTUÉ",
    val timestamp: Long = System.currentTimeMillis()
)

object MonetizationManager {

    private const val TAG = "MonetizationManager"

    // Default In-Memory State for Instant Responsiveness
    var config: PlatformConfig = PlatformConfig()
    val revenueEntries = mutableListOf<RevenueEntry>()
    val activeSubscriptions = mutableMapOf<String, SellerSubscription>()
    val activeBoosts = mutableMapOf<String, BoostedProductRecord>()
    val payoutHistory = mutableListOf<PayoutRequest>()

    init {
        // Seed initial realistic monetization baseline data
        revenueEntries.add(
            RevenueEntry(
                source = "SUBSCRIPTION",
                amount = 12500.0,
                description = "Abonnement VIP Diamant - Boutique Douala Fashion",
                referenceId = "SUB-DLA-01",
                paymentOperator = "Orange Money",
                timestamp = System.currentTimeMillis() - 86400000 * 3
            )
        )
        revenueEntries.add(
            RevenueEntry(
                source = "COMMISSION",
                amount = 3250.0,
                description = "Commission vente (5%) - Commande #CMD-84920",
                referenceId = "CMD-84920",
                paymentOperator = "MTN MoMo",
                timestamp = System.currentTimeMillis() - 86400000 * 2
            )
        )
        revenueEntries.add(
            RevenueEntry(
                source = "BOOST",
                amount = 2800.0,
                description = "Mega Boost VIP 14J - iPhone 14 Pro Max",
                referenceId = "PROD-IP14",
                paymentOperator = "Orange Money",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
        revenueEntries.add(
            RevenueEntry(
                source = "SERVICE_FEE",
                amount = 200.0,
                description = "Frais de protection acheteur sécurisée - Commande #CMD-92140",
                referenceId = "CMD-92140",
                paymentOperator = "Orange Money",
                timestamp = System.currentTimeMillis() - 3600000 * 5
            )
        )
    }

    /**
     * Calculates order breakdown:
     * - Buyer Service Fee
     * - Platform Commission on seller items
     * - Net Seller Payout
     */
    fun calculateOrderBreakdown(itemsTotal: Double, sellerTier: SubscriptionTier = SubscriptionTier.FREE): Triple<Double, Double, Double> {
        val serviceFee = config.platformBuyerServiceFee
        val commissionRate = sellerTier.commissionRatePercent
        val commission = (itemsTotal * commissionRate) / 100.0
        val sellerNet = itemsTotal - commission
        return Triple(serviceFee, commission, sellerNet)
    }

    /**
     * Records a new revenue transaction in memory and Firestore
     */
    suspend fun recordRevenue(
        source: String,
        amount: Double,
        description: String,
        referenceId: String,
        paymentOperator: String
    ) {
        val entry = RevenueEntry(
            source = source,
            amount = amount,
            description = description,
            referenceId = referenceId,
            paymentOperator = paymentOperator
        )
        revenueEntries.add(0, entry)

        try {
            val db = com.example.data.firebase.FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext)
            db.collection("platform_revenues").document(entry.id).set(
                hashMapOf(
                    "id" to entry.id,
                    "source" to entry.source,
                    "amount" to entry.amount,
                    "description" to entry.description,
                    "referenceId" to entry.referenceId,
                    "paymentOperator" to entry.paymentOperator,
                    "timestamp" to entry.timestamp
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync revenue entry to Firestore: ${e.message}")
        }
    }

    /**
     * Activates a subscription for a seller upon payment
     */
    suspend fun subscribeSeller(
        sellerId: String,
        sellerName: String,
        tier: SubscriptionTier,
        operator: PaymentOperator,
        txnId: String
    ): SellerSubscription {
        val subscription = SellerSubscription(
            sellerId = sellerId,
            sellerName = sellerName,
            tier = tier,
            startDate = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (tier.durationDays.toLong() * 24 * 60 * 60 * 1000),
            active = true,
            paymentTxnId = txnId
        )
        activeSubscriptions[sellerId] = subscription

        recordRevenue(
            source = "SUBSCRIPTION",
            amount = tier.priceXaf,
            description = "Abonnement ${tier.title} - $sellerName",
            referenceId = sellerId,
            paymentOperator = operator.displayName
        )

        try {
            val db = com.example.data.firebase.FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext)
            db.collection("seller_subscriptions").document(sellerId).set(
                hashMapOf(
                    "sellerId" to subscription.sellerId,
                    "sellerName" to subscription.sellerName,
                    "tier" to subscription.tier.name,
                    "tierTitle" to subscription.tier.title,
                    "startDate" to subscription.startDate,
                    "expiresAt" to subscription.expiresAt,
                    "active" to subscription.active,
                    "paymentTxnId" to subscription.paymentTxnId
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore subscription sync error: ${e.message}")
        }

        return subscription
    }

    /**
     * Activates a boost package for a product
     */
    suspend fun boostProduct(
        productId: String,
        productName: String,
        shopName: String,
        boostPackage: BoostPackage,
        operator: PaymentOperator,
        txnId: String
    ): BoostedProductRecord {
        val record = BoostedProductRecord(
            productId = productId,
            productName = productName,
            shopName = shopName,
            boostPackage = boostPackage,
            startDate = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (boostPackage.durationDays.toLong() * 24 * 60 * 60 * 1000),
            active = true,
            paymentTxnId = txnId
        )
        activeBoosts[productId] = record

        recordRevenue(
            source = "BOOST",
            amount = boostPackage.priceXaf,
            description = "${boostPackage.title} - $productName ($shopName)",
            referenceId = productId,
            paymentOperator = operator.displayName
        )

        try {
            val db = com.example.data.firebase.FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext)
            db.collection("boosted_products").document(productId).set(
                hashMapOf(
                    "productId" to record.productId,
                    "productName" to record.productName,
                    "shopName" to record.shopName,
                    "boostPackage" to record.boostPackage.name,
                    "startDate" to record.startDate,
                    "expiresAt" to record.expiresAt,
                    "active" to record.active,
                    "paymentTxnId" to record.paymentTxnId
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore boost sync error: ${e.message}")
        }

        return record
    }

    /**
     * Admin Payout Request to withdraw platform earnings to Orange Money or MTN MoMo
     */
    suspend fun requestAdminPayout(
        amount: Double,
        phone: String,
        operator: PaymentOperator,
        country: CentralAfricanCountry
    ): PayoutRequest {
        val payout = PayoutRequest(
            amount = amount,
            recipientPhone = phone,
            operator = operator,
            country = country,
            status = "VIRÉ AVEC SUCCÈS",
            timestamp = System.currentTimeMillis()
        )
        payoutHistory.add(0, payout)

        try {
            val db = com.example.data.firebase.FirestoreProvider.get(com.google.firebase.FirebaseApp.getInstance().applicationContext)
            db.collection("admin_payouts").document(payout.id).set(
                hashMapOf(
                    "id" to payout.id,
                    "amount" to payout.amount,
                    "recipientPhone" to payout.recipientPhone,
                    "operator" to payout.operator.name,
                    "country" to payout.country.countryName,
                    "status" to payout.status,
                    "timestamp" to payout.timestamp
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore payout sync error: ${e.message}")
        }

        return payout
    }

    fun getTotalPlatformRevenue(): Double {
        return revenueEntries.sumOf { it.amount }
    }

    fun getTotalWithdrawn(): Double {
        return payoutHistory.sumOf { it.amount }
    }

    fun getAvailableBalance(): Double {
        return (getTotalPlatformRevenue() - getTotalWithdrawn()).coerceAtLeast(0.0)
    }
}
