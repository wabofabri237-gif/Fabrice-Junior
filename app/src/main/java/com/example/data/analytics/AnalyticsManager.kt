package com.example.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.domain.model.CartItem
import com.example.domain.model.Product
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsEventRecord(
    val id: String = "EVT-${System.currentTimeMillis()}-${(100..999).random()}",
    val eventName: String,
    val category: String, // E-COMMERCE, SEARCH, PAYMENT, SELLER, NAVIGATION
    val details: String,
    val parameters: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConversionFunnelMetrics(
    val productViewsCount: Int = 0,
    val addToCartCount: Int = 0,
    val checkoutCount: Int = 0,
    val purchaseSuccessCount: Int = 0,
    val totalSalesVolumeXaf: Double = 0.0,
    val viewToCartRate: Double = 0.0, // (cart / views) * 100
    val cartToCheckoutRate: Double = 0.0, // (checkout / cart) * 100
    val checkoutToPaymentRate: Double = 0.0, // (purchases / checkout) * 100
    val overallConversionRate: Double = 0.0 // (purchases / views) * 100
)

data class SearchTrend(
    val query: String,
    val count: Int,
    val lastCategory: String,
    val hasResults: Boolean
)

data class CategoryPopularity(
    val category: String,
    val viewCount: Int,
    val purchaseCount: Int,
    val conversionRate: Double
)

object AnalyticsManager {
    private const val TAG = "NboaAnalytics"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    // Live state flows for real-time dashboard observation
    private val _eventLogs = MutableStateFlow<List<AnalyticsEventRecord>>(emptyList())
    val eventLogs: StateFlow<List<AnalyticsEventRecord>> = _eventLogs.asStateFlow()

    // Aggregated metrics
    private val _funnelMetrics = MutableStateFlow(
        ConversionFunnelMetrics(
            productViewsCount = 42,
            addToCartCount = 28,
            checkoutCount = 19,
            purchaseSuccessCount = 14,
            totalSalesVolumeXaf = 385000.0,
            viewToCartRate = 66.7,
            cartToCheckoutRate = 67.8,
            checkoutToPaymentRate = 73.7,
            overallConversionRate = 33.3
        )
    )
    val funnelMetrics: StateFlow<ConversionFunnelMetrics> = _funnelMetrics.asStateFlow()

    // Search query tracker
    private val searchStatsMap = mutableMapOf<String, Int>(
        "iPhone 13" to 15,
        "Robe Wax" to 12,
        "Sneakers Nike" to 10,
        "Plantain frais" to 8,
        "MacBook Pro" to 7,
        "Huile de palme" to 6,
        "Smart TV 4K" to 5
    )

    // Category visits
    private val categoryStatsMap = mutableMapOf<String, Pair<Int, Int>>(
        "Électronique & High-Tech" to Pair(35, 9),
        "Mode & Vêtements" to Pair(28, 7),
        "Alimentation & Vivres" to Pair(22, 6),
        "Beauté & Cosmétiques" to Pair(18, 4),
        "Maison & Déco" to Pair(14, 3),
        "Services & Freelance" to Pair(9, 2)
    )

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Analytics init skipped or running without Google Play Services: ${e.message}")
        }
    }

    private fun logFirebaseEvent(name: String, bundle: Bundle) {
        try {
            firebaseAnalytics?.logEvent(name, bundle)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log event '$name' to Firebase: ${e.message}")
        }
    }

    private fun recordLocalEvent(
        eventName: String,
        category: String,
        details: String,
        params: Map<String, String>
    ) {
        val record = AnalyticsEventRecord(
            eventName = eventName,
            category = category,
            details = details,
            parameters = params
        )
        _eventLogs.value = (listOf(record) + _eventLogs.value).take(100)
    }

    // 1. Search Behavior Tracking
    fun trackSearch(query: String, categoryFilter: String? = null, resultsCount: Int = 0) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, trimmed)
            putString("category_filter", categoryFilter ?: "All")
            putInt("results_count", resultsCount)
        }
        logFirebaseEvent(FirebaseAnalytics.Event.SEARCH, bundle)

        searchStatsMap[trimmed] = (searchStatsMap[trimmed] ?: 0) + 1

        recordLocalEvent(
            eventName = "search_query",
            category = "SEARCH",
            details = "Recherche: \"$trimmed\" ($resultsCount résultats)",
            params = mapOf(
                "query" to trimmed,
                "category" to (categoryFilter ?: "Tous"),
                "results_count" to resultsCount.toString()
            )
        )
    }

    // 2. Category Viewing & Filtering
    fun trackCategoryView(categoryName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, categoryName)
        }
        logFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle)

        val current = categoryStatsMap[categoryName] ?: Pair(0, 0)
        categoryStatsMap[categoryName] = Pair(current.first + 1, current.second)

        recordLocalEvent(
            eventName = "view_category",
            category = "NAVIGATION",
            details = "Consultation de la catégorie: $categoryName",
            params = mapOf("category" to categoryName)
        )
    }

    // 3. Product Details View (Step 1 of Funnel)
    fun trackProductView(product: Product) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, product.id)
            putString(FirebaseAnalytics.Param.ITEM_NAME, product.name)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, product.category)
            putDouble(FirebaseAnalytics.Param.PRICE, product.price)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
            putString("seller_name", product.shopName)
            putBoolean("is_sponsored", product.isSponsored)
        }
        logFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)

        val current = categoryStatsMap[product.category] ?: Pair(0, 0)
        categoryStatsMap[product.category] = Pair(current.first + 1, current.second)

        val currentFunnel = _funnelMetrics.value
        val newViews = currentFunnel.productViewsCount + 1
        recalculateFunnel(views = newViews)

        recordLocalEvent(
            eventName = "view_product",
            category = "E-COMMERCE",
            details = "Vue Produit: ${product.name} (${product.price.toInt()} XAF)",
            params = mapOf(
                "product_id" to product.id,
                "product_name" to product.name,
                "category" to product.category,
                "price" to "${product.price.toInt()} XAF",
                "sponsored" to product.isSponsored.toString()
            )
        )
    }

    // 4. Add to Cart (Step 2 of Funnel)
    fun trackAddToCart(product: Product, quantity: Int = 1) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, product.id)
            putString(FirebaseAnalytics.Param.ITEM_NAME, product.name)
            putString(FirebaseAnalytics.Param.ITEM_CATEGORY, product.category)
            putDouble(FirebaseAnalytics.Param.PRICE, product.price)
            putLong(FirebaseAnalytics.Param.QUANTITY, quantity.toLong())
            putDouble(FirebaseAnalytics.Param.VALUE, product.price * quantity)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
        }
        logFirebaseEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle)

        val currentFunnel = _funnelMetrics.value
        val newCart = currentFunnel.addToCartCount + 1
        recalculateFunnel(cart = newCart)

        recordLocalEvent(
            eventName = "add_to_cart",
            category = "E-COMMERCE",
            details = "Ajout Panier: ${product.name} (Qté: $quantity)",
            params = mapOf(
                "product_id" to product.id,
                "product_name" to product.name,
                "quantity" to quantity.toString(),
                "total_val" to "${(product.price * quantity).toInt()} XAF"
            )
        )
    }

    // 5. Begin Checkout (Step 3 of Funnel)
    fun trackBeginCheckout(items: List<CartItem>, totalAmount: Double, country: String) {
        val bundle = Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, totalAmount)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
            putInt(FirebaseAnalytics.Param.ITEMS, items.size)
            putString("country", country)
        }
        logFirebaseEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle)

        val currentFunnel = _funnelMetrics.value
        val newCheckout = currentFunnel.checkoutCount + 1
        recalculateFunnel(checkout = newCheckout)

        recordLocalEvent(
            eventName = "begin_checkout",
            category = "E-COMMERCE",
            details = "Début Commande: ${items.size} articles (${totalAmount.toInt()} XAF)",
            params = mapOf(
                "items_count" to items.size.toString(),
                "total_amount" to "${totalAmount.toInt()} XAF",
                "country" to country
            )
        )
    }

    // 6. Payment Flow & Final Purchase Conversion (Step 4 of Funnel)
    fun trackPurchase(
        orderId: String,
        totalAmount: Double,
        itemCount: Int,
        paymentProvider: String,
        country: String,
        category: String? = null
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.TRANSACTION_ID, orderId)
            putDouble(FirebaseAnalytics.Param.VALUE, totalAmount)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
            putString(FirebaseAnalytics.Param.PAYMENT_TYPE, paymentProvider)
            putString("country", country)
            putInt("item_count", itemCount)
        }
        logFirebaseEvent(FirebaseAnalytics.Event.PURCHASE, bundle)

        if (category != null) {
            val current = categoryStatsMap[category] ?: Pair(0, 0)
            categoryStatsMap[category] = Pair(current.first, current.second + 1)
        }

        val currentFunnel = _funnelMetrics.value
        val newPurchases = currentFunnel.purchaseSuccessCount + 1
        val newVolume = currentFunnel.totalSalesVolumeXaf + totalAmount
        recalculateFunnel(purchases = newPurchases, volume = newVolume)

        recordLocalEvent(
            eventName = "purchase_success",
            category = "PAYMENT",
            details = "Paiement Réussi: Commande #$orderId (${totalAmount.toInt()} XAF via $paymentProvider)",
            params = mapOf(
                "order_id" to orderId,
                "amount" to "${totalAmount.toInt()} XAF",
                "provider" to paymentProvider,
                "country" to country
            )
        )
    }

    // 7. Seller Monetization Events
    fun trackSellerSubscription(sellerId: String, tier: String, priceXaf: Double, operator: String) {
        val bundle = Bundle().apply {
            putString("seller_id", sellerId)
            putString("subscription_tier", tier)
            putDouble(FirebaseAnalytics.Param.VALUE, priceXaf)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
            putString("payment_operator", operator)
        }
        logFirebaseEvent("seller_subscription_upgraded", bundle)

        recordLocalEvent(
            eventName = "subscription_upgrade",
            category = "SELLER",
            details = "Souscription Pro: $tier (${priceXaf.toInt()} XAF via $operator)",
            params = mapOf(
                "seller_id" to sellerId,
                "tier" to tier,
                "price" to "${priceXaf.toInt()} XAF",
                "operator" to operator
            )
        )
    }

    fun trackProductBoost(productId: String, productName: String, boostPackage: String, priceXaf: Double) {
        val bundle = Bundle().apply {
            putString("product_id", productId)
            putString("product_name", productName)
            putString("boost_package", boostPackage)
            putDouble(FirebaseAnalytics.Param.VALUE, priceXaf)
            putString(FirebaseAnalytics.Param.CURRENCY, "XAF")
        }
        logFirebaseEvent("product_boost_activated", bundle)

        recordLocalEvent(
            eventName = "boost_product",
            category = "SELLER",
            details = "Boost Produit: $productName ($boostPackage - ${priceXaf.toInt()} XAF)",
            params = mapOf(
                "product_id" to productId,
                "product" to productName,
                "package" to boostPackage,
                "price" to "${priceXaf.toInt()} XAF"
            )
        )
    }

    // 8. User Properties
    fun setUserProperties(role: String, country: String, isProSeller: Boolean = false) {
        try {
            firebaseAnalytics?.setUserProperty("user_role", role)
            firebaseAnalytics?.setUserProperty("user_country", country)
            firebaseAnalytics?.setUserProperty("is_pro_seller", isProSeller.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed setting user properties: ${e.message}")
        }
    }

    private fun recalculateFunnel(
        views: Int? = null,
        cart: Int? = null,
        checkout: Int? = null,
        purchases: Int? = null,
        volume: Double? = null
    ) {
        val current = _funnelMetrics.value
        val v = views ?: current.productViewsCount
        val c = cart ?: current.addToCartCount
        val chk = checkout ?: current.checkoutCount
        val p = purchases ?: current.purchaseSuccessCount
        val vol = volume ?: current.totalSalesVolumeXaf

        val vToCart = if (v > 0) (c.toDouble() / v.toDouble()) * 100.0 else 0.0
        val cToChk = if (c > 0) (chk.toDouble() / c.toDouble()) * 100.0 else 0.0
        val chkToPay = if (chk > 0) (p.toDouble() / chk.toDouble()) * 100.0 else 0.0
        val overall = if (v > 0) (p.toDouble() / v.toDouble()) * 100.0 else 0.0

        _funnelMetrics.value = ConversionFunnelMetrics(
            productViewsCount = v,
            addToCartCount = c,
            checkoutCount = chk,
            purchaseSuccessCount = p,
            totalSalesVolumeXaf = vol,
            viewToCartRate = (vToCart * 10).toInt() / 10.0,
            cartToCheckoutRate = (cToChk * 10).toInt() / 10.0,
            checkoutToPaymentRate = (chkToPay * 10).toInt() / 10.0,
            overallConversionRate = (overall * 10).toInt() / 10.0
        )
    }

    fun getPopularSearches(): List<SearchTrend> {
        return searchStatsMap.entries
            .sortedByDescending { it.value }
            .map {
                SearchTrend(
                    query = it.key,
                    count = it.value,
                    lastCategory = "Général",
                    hasResults = true
                )
            }
    }

    fun getCategoryPopularityList(): List<CategoryPopularity> {
        return categoryStatsMap.entries
            .sortedByDescending { it.value.first }
            .map { (cat, counts) ->
                val views = counts.first
                val buys = counts.second
                val rate = if (views > 0) (buys.toDouble() / views.toDouble()) * 100.0 else 0.0
                CategoryPopularity(
                    category = cat,
                    viewCount = views,
                    purchaseCount = buys,
                    conversionRate = (rate * 10).toInt() / 10.0
                )
            }
    }
}
