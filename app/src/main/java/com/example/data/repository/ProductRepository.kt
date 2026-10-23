package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreProvider
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.domain.model.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirestoreProvider.get(context))

    val defaultCatalog = listOf(
        Product(
            id = "prod_sac_art",
            sellerId = "seller_official",
            name = "Sac à main artisanal en cuir et raphia",
            title = "Sac à main artisanal en cuir et raphia",
            description = "Fait main à Douala avec des matériaux nobles du terroir. Finition soignée et bandoulière réglable.",
            price = 15000.0,
            currency = "XAF",
            shopName = "La Boutique de Marie",
            imageUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa",
            category = "Maroquinerie",
            inStock = true,
            stockQuantity = 15
        ),
        Product(
            id = "prod_chauss_cuir",
            sellerId = "seller_official",
            name = "Mocassins en cuir véritable",
            title = "Mocassins en cuir véritable",
            description = "Chaussures homme et femme grand confort. Cuir pleine fleur tanné localement, semelle antidérapante.",
            price = 25000.0,
            currency = "XAF",
            shopName = "Cuir d'Afrique",
            imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2",
            category = "Chaussures",
            inStock = true,
            stockQuantity = 20
        ),
        Product(
            id = "prod_robe_pagne",
            sellerId = "seller_official",
            name = "Robe d'apparat en pagne Toghu brodé",
            title = "Robe d'apparat en pagne Toghu brodé",
            description = "Motifs royaux des Grassfields du Nord-Ouest. Coupe élégante pour cérémonies et mariages.",
            price = 35000.0,
            currency = "XAF",
            shopName = "Mode Kmer",
            imageUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b",
            category = "Mode Africaine",
            inStock = true,
            stockQuantity = 8
        ),
        Product(
            id = "prod_cafe_ouest",
            sellerId = "seller_official",
            name = "Café Arabica Pur de l'Ouest",
            title = "Café Arabica Pur de l'Ouest",
            description = "Grains torréfiés sur les hauts plateaux de Bafoussam. Arôme intense et notes chocolatées.",
            price = 4500.0,
            currency = "XAF",
            shopName = "Saveurs du Terroir",
            imageUrl = "https://images.unsplash.com/photo-1559056199-641a0ac8b55e",
            category = "Épicerie Locale",
            inStock = true,
            stockQuantity = 50
        ),
        Product(
            id = "prod_savon_noir",
            sellerId = "seller_official",
            name = "Savon Noir purifiant au beurre de karité",
            title = "Savon Noir purifiant au beurre de karité",
            description = "Soin dermatologique 100% bio. Élimine les impuretés et nourrit la peau en profondeur.",
            price = 3000.0,
            currency = "XAF",
            shopName = "Cosmétiques Bio Nboa",
            imageUrl = "https://images.unsplash.com/photo-1608248597359-009772a445d4",
            category = "Beauté & Soins",
            inStock = true,
            stockQuantity = 40
        )
    )

    fun observeProducts(): Flow<List<Product>> {
        return db.collection("products")
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(Product::class.java)
                if (list.isNotEmpty()) list else defaultCatalog
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "products")
                emit(defaultCatalog)
            }
    }

    suspend fun createProduct(
        sellerId: String,
        title: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        shopName: String = "Ma Boutique"
    ): Result<String> {
        val prodId = "prod_" + UUID.randomUUID().toString().take(10)
        val data = hashMapOf<String, Any>(
            "id" to prodId,
            "sellerId" to sellerId,
            "title" to title,
            "description" to description,
            "price" to price.toLong(),
            "currency" to "XAF",
            "category" to category,
            "imageUrl" to imageUrl,
            "inStock" to true,
            "stockQuantity" to 10,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            db.collection("products").document(prodId).set(data).await()
            Result.success(prodId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "products/$prodId")
            Result.failure(e)
        }
    }
}
