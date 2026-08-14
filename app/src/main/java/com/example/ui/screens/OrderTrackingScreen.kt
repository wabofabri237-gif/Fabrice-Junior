package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var ordersList by remember { mutableStateOf<List<FirestoreOrder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time listener from Firestore for order tracking
    DisposableEffect(Unit) {
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        try {
            val db = FirebaseFirestore.getInstance()
            listener = db.collection("orders")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error != null) {
                        errorMessage = "Mode hors-ligne / Synchronisation Firestore en attente"
                        ordersList = listOf(
                            FirestoreOrder(
                                id = "CMD-84920",
                                customerName = "Jean Dupont",
                                status = "IN_TRANSIT",
                                totalAmount = 25000.0,
                                deliveryAddress = "Bonanjo, Douala (+237)",
                                timestamp = System.currentTimeMillis() - 3600000,
                                items = listOf(FirestoreOrderItem("Sac à main artisanal", 15000.0, 1), FirestoreOrderItem("Écouteurs sans fil", 10000.0, 1))
                            ),
                            FirestoreOrder(
                                id = "CMD-73192",
                                customerName = "Marie Claire",
                                status = "PREPARING",
                                totalAmount = 18000.0,
                                deliveryAddress = "Akwa, Douala (+237)",
                                timestamp = System.currentTimeMillis() - 1800000,
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
                    status = "IN_TRANSIT",
                    totalAmount = 25000.0,
                    deliveryAddress = "Bonanjo, Douala (+237)",
                    timestamp = System.currentTimeMillis() - 3600000,
                    items = listOf(FirestoreOrderItem("Sac à main", 15000.0, 1))
                )
            )
        }

        onDispose {
            try {
                listener?.remove()
            } catch (e: Exception) {}
        }
    }

    // Function to update status in Firestore in real-time
    val updateOrderStatus: (String, String) -> Unit = { orderId, newStatus ->
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(context, "Statut mis à jour : $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Update local state fallback if offline
                    ordersList = ordersList.map { if (it.id == orderId) it.copy(status = newStatus) else it }
                    Toast.makeText(context, "Mis à jour localement (Hors-ligne)", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            ordersList = ordersList.map { if (it.id == orderId) it.copy(status = newStatus) else it }
            Toast.makeText(context, "Mis à jour localement", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suivi des Commandes en Temps Réel", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                    Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("Aucune commande en cours de suivi.", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📍 Suivi GPS & Chronologie Firestore", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Suivez l'acheminement de vos colis en temps réel à travers la région CEMAC. Les changements de statut sont instantanés.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    items(ordersList) { order ->
                        TrackingOrderCard(order = order, onUpdateStatus = updateOrderStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingOrderCard(
    order: FirestoreOrder,
    onUpdateStatus: (String, String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(order.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            Text(
                text = "Client : ${order.customerName} | 📍 ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Visual Timeline Section
            Text(
                text = "Chronologie de Livraison :",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Timeline steps
            val currentStatus = order.status
            val steps = listOf(
                Triple("PAID", "Payé & Validé", Icons.Default.CheckCircle),
                Triple("PREPARING", "En Préparation", Icons.Default.Inventory),
                Triple("IN_TRANSIT", "En Livraison", Icons.Default.LocalShipping),
                Triple("DELIVERED", "Livré", Icons.Default.CheckCircle)
            )

            // Determine active step index
            val currentIndex = when (currentStatus) {
                "PAID" -> 0
                "PREPARING" -> 1
                "IN_TRANSIT" -> 2
                "DELIVERED" -> 3
                else -> 0
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                steps.forEachIndexed { index, step ->
                    val isCompleted = index <= currentIndex
                    val isCurrent = index == currentIndex
                    val stepColor = if (isCompleted) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon circle
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else stepColor,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageAsIcon(step.third),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.second,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }

                        if (isCurrent) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Actuel",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.8.dp)

            // Action Buttons to advance status (for testing / live updates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentStatus != "DELIVERED") {
                    val nextStatus = when (currentStatus) {
                        "PAID" -> "PREPARING"
                        "PREPARING" -> "IN_TRANSIT"
                        "IN_TRANSIT" -> "DELIVERED"
                        else -> "DELIVERED"
                    }
                    val nextLabel = when (nextStatus) {
                        "PREPARING" -> "Passer en Préparation"
                        "IN_TRANSIT" -> "Lancer la Livraison"
                        "DELIVERED" -> "Marquer Livré"
                        else -> "Avancer"
                    }

                    Button(
                        onClick = { onUpdateStatus(order.id, nextStatus) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(nextLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Commande livrée avec succès",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun imageAsIcon(vector: androidx.compose.ui.graphics.vector.ImageVector): androidx.compose.ui.graphics.vector.ImageVector {
    return vector
}
