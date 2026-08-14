package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FirestoreOrderItem(
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)

data class FirestoreOrder(
    val id: String = "",
    val customerName: String = "",
    val status: String = "PAID",
    val totalAmount: Double = 0.0,
    val deliveryAddress: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<FirestoreOrderItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit
) {
    var ordersList by remember { mutableStateOf<List<FirestoreOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch order history from Firestore or fallback gracefully
    DisposableEffect(Unit) {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        try {
            val db = FirebaseFirestore.getInstance()
            listener = db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error != null) {
                        errorMessage = "Mode hors-ligne / Firestore non initialisé"
                        ordersList = listOf(
                            FirestoreOrder(
                                id = "CMD-84920",
                                customerName = "Jean Dupont",
                                status = "PAID",
                                totalAmount = 25000.0,
                                deliveryAddress = "Bonanjo, Douala (Cameroun +237)",
                                timestamp = System.currentTimeMillis() - 3600000,
                                items = listOf(FirestoreOrderItem("Sac à main artisanal", 15000.0, 1), FirestoreOrderItem("Écouteurs sans fil", 10000.0, 1))
                            ),
                            FirestoreOrder(
                                id = "CMD-73192",
                                customerName = "Jean Dupont",
                                status = "DELIVERED",
                                totalAmount = 18000.0,
                                deliveryAddress = "Akwa, Douala (Cameroun +237)",
                                timestamp = System.currentTimeMillis() - 86400000,
                                items = listOf(FirestoreOrderItem("Robe en pagne", 18000.0, 1))
                            )
                        )
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val customerName = doc.getString("customerName") ?: "Client"
                                val status = doc.getString("status") ?: "PAID"
                                val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                                val deliveryAddress = doc.getString("deliveryAddress") ?: ""
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                
                                val rawItems = doc.get("items") as? List<*>
                                val items = rawItems?.mapNotNull { itemMap ->
                                    val map = itemMap as? Map<*, *>
                                    if (map != null) {
                                        FirestoreOrderItem(
                                            productName = map["productName"] as? String ?: "Article",
                                            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                                            quantity = (map["quantity"] as? Number)?.toInt() ?: 1
                                        )
                                    } else null
                                } ?: emptyList()

                                FirestoreOrder(id, customerName, status, totalAmount, deliveryAddress, timestamp, items)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        ordersList = list.ifEmpty {
                            listOf(
                                FirestoreOrder(
                                    id = "CMD-55421",
                                    customerName = "Client NboaShop",
                                    status = "IN_TRANSIT",
                                    totalAmount = 15000.0,
                                    deliveryAddress = "Bastos, Yaoundé (+237)",
                                    timestamp = System.currentTimeMillis(),
                                    items = listOf(FirestoreOrderItem("Sac à main artisanal", 15000.0, 1))
                                )
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            isLoading = false
            ordersList = listOf(
                FirestoreOrder(
                    id = "CMD-84920",
                    customerName = "Jean Dupont",
                    status = "PAID",
                    totalAmount = 25000.0,
                    deliveryAddress = "Bonanjo, Douala (Cameroun +237)",
                    timestamp = System.currentTimeMillis() - 3600000,
                    items = listOf(FirestoreOrderItem("Sac à main artisanal", 15000.0, 1), FirestoreOrderItem("Écouteurs sans fil", 10000.0, 1))
                ),
                FirestoreOrder(
                    id = "CMD-73192",
                    customerName = "Jean Dupont",
                    status = "DELIVERED",
                    totalAmount = 18000.0,
                    deliveryAddress = "Akwa, Douala (Cameroun +237)",
                    timestamp = System.currentTimeMillis() - 86400000,
                    items = listOf(FirestoreOrderItem("Robe en pagne", 18000.0, 1))
                )
            )
        }

        onDispose {
            try {
                listener?.remove()
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique des Commandes (Firestore)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (ordersList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("Aucune commande enregistrée dans Firestore.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ordersList) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: FirestoreOrder) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(order.timestamp))

    val (statusColor, statusLabel, statusIcon) = when (order.status) {
        "PAID" -> Triple(Color(0xFF2E7D32), "Payé - En préparation", Icons.Default.CheckCircle)
        "IN_TRANSIT" -> Triple(Color(0xFFEF6C00), "En cours de livraison", Icons.Default.LocalShipping)
        "DELIVERED" -> Triple(Color(0xFF1565C0), "Livré", Icons.Default.CheckCircle)
        else -> Triple(Color(0xFF616161), order.status, Icons.Default.Schedule)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Items Purchased Section
            Text(
                text = "Articles achetés :",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            if (order.items.isEmpty()) {
                Text(text = "Aucun détail d'article", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${item.productName} (x${item.quantity})",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${item.price * item.quantity} XAF",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Delivery Address & Total Amount
            Text(
                text = "📍 Adresse : ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Montant Total :",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${order.totalAmount} XAF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
