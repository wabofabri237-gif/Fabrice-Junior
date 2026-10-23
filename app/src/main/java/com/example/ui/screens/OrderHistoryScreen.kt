package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.OrderRepository
import com.example.domain.model.Order
import com.example.domain.model.OrderStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OrderFilterCategory(val label: String) {
    ALL("Toutes"),
    ACTIVE("En cours"),
    DELIVERED("Livrées"),
    CANCELLED("Annulées")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    val orderRepository = remember { OrderRepository(context) }
    val userId = currentUser?.uid ?: ""

    var selectedFilter by remember { mutableStateOf(OrderFilterCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Stream orders from Firestore in real-time
    val ordersState by remember(userId) {
        orderRepository.observeUserOrders(userId)
    }.collectAsState(initial = null)

    val ordersList: List<Order>? = ordersState

    // Filter and search logic
    val filteredOrders = remember(ordersList, selectedFilter, searchQuery) {
        val list = ordersList ?: emptyList()
        list.filter { order ->
            val matchesCategory = when (selectedFilter) {
                OrderFilterCategory.ALL -> true
                OrderFilterCategory.ACTIVE -> order.status in listOf(
                    OrderStatus.PENDING,
                    OrderStatus.PAID,
                    OrderStatus.PROCESSING,
                    OrderStatus.CONFIRMED,
                    OrderStatus.IN_TRANSIT
                )
                OrderFilterCategory.DELIVERED -> order.status == OrderStatus.DELIVERED
                OrderFilterCategory.CANCELLED -> order.status == OrderStatus.CANCELLED
            }

            val q = searchQuery.trim().lowercase()
            val matchesSearch = if (q.isEmpty()) true else {
                order.id.lowercase().contains(q) ||
                        order.itemsSummary.lowercase().contains(q) ||
                        order.deliveryCity.lowercase().contains(q) ||
                        order.deliveryAddress.lowercase().contains(q) ||
                        order.bankAccountName.lowercase().contains(q) ||
                        order.bankTransferRef.lowercase().contains(q)
            }

            matchesCategory && matchesSearch
        }
    }

    val totalSpent = remember(ordersList) {
        ordersList?.filter { it.status != OrderStatus.CANCELLED }?.sumOf { it.totalAmount } ?: 0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Historique des Commandes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (currentUser != null) {
                            Text(
                                text = currentUser.displayName ?: currentUser.email ?: "Compte vérifié",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retourner à la boutique"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Rechercher une commande"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar (Animatable)
            AnimatedVisibility(visible = isSearchExpanded) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("search_input"),
                        placeholder = { Text("Rechercher par réf., produit, ville...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Authentication Warning if not signed in
            if (currentUser == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connexion requise",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connectez-vous avec votre compte Google pour accéder à votre historique de commandes synchronisé avec Firestore.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else {
                // Filter Tabs / Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(OrderFilterCategory.entries.toTypedArray()) { category ->
                        val isSelected = selectedFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = category },
                            label = {
                                Text(
                                    text = when (category) {
                                        OrderFilterCategory.ALL -> "${category.label} (${ordersList?.size ?: 0})"
                                        else -> category.label
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                        )
                    }
                }

                // Summary Row
                if (ordersList != null && ordersList.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredOrders.size} commande(s) affichée(s)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Cumul : ${"%,.0f".format(totalSpent)} XAF",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Main Content List / States
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp)
                ) {
                    when {
                        ordersList == null -> {
                            // Loading state
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Chargement des commandes depuis Firestore...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        ordersList.isEmpty() -> {
                            // Empty State
                            EmptyOrdersView(
                                title = "Aucune commande passée",
                                message = "Vous n'avez pas encore passé de commande sur NboaShop. Vos commandes au paiement à la livraison (COD) et virement bancaire local s'afficheront ici en direct.",
                                onActionClick = onBackClick
                            )
                        }
                        filteredOrders.isEmpty() -> {
                            // No search / filter results
                            EmptyOrdersView(
                                title = "Aucun résultat trouvé",
                                message = "Aucune commande ne correspond aux filtres ou à la recherche courante.",
                                onActionClick = {
                                    searchQuery = ""
                                    selectedFilter = OrderFilterCategory.ALL
                                },
                                actionText = "Réinitialiser les filtres"
                            )
                        }
                        else -> {
                            // List of Orders
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("orders_list"),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = filteredOrders,
                                    key = { it.id.ifBlank { it.timestamp.toString() } }
                                ) { order ->
                                    PastOrderCard(order = order, context = context)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PastOrderCard(order: Order, context: Context) {
    var isExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH) }
    val formattedDate = remember(order.timestamp) {
        try {
            dateFormat.format(Date(order.timestamp))
        } catch (e: Exception) {
            "Récemment"
        }
    }

    val (statusColor, statusBgColor, statusLabel, statusIcon) = when (order.status) {
        OrderStatus.PENDING -> Quad(
            Color(0xFFE65100),
            Color(0xFFFFF3E0),
            "En attente de confirmation",
            Icons.Default.HourglassEmpty
        )
        OrderStatus.PAID, OrderStatus.PROCESSING -> Quad(
            Color(0xFF0D47A1),
            Color(0xFFE3F2FD),
            "En préparation marchand",
            Icons.Default.Autorenew
        )
        OrderStatus.CONFIRMED -> Quad(
            Color(0xFF00695C),
            Color(0xFFE0F2F1),
            "Commande confirmée",
            Icons.Default.CheckCircle
        )
        OrderStatus.IN_TRANSIT -> Quad(
            Color(0xFFF57C00),
            Color(0xFFFFF8E1),
            "En cours de livraison",
            Icons.Default.LocalShipping
        )
        OrderStatus.DELIVERED -> Quad(
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9),
            "Livré avec succès",
            Icons.Default.CheckCircle
        )
        OrderStatus.CANCELLED -> Quad(
            Color(0xFFC62828),
            Color(0xFFFFEBEE),
            "Commande annulée",
            Icons.Default.Cancel
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: ID and Copy Button + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "#${order.id}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ID Commande", order.id)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Réf. #${order.id} copiée !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("copy_btn_${order.id}")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copier la référence",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Badge
            Surface(
                color = statusBgColor,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Transaction & Payment Details Box
            if (order.paymentMethod == "CASH_ON_DELIVERY") {
                // Cash on Delivery Card with highlighted OTP
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Paiement à la livraison (Cash on Delivery)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        // OTP Display Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Code OTP de remise :",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "À communiquer au livreur",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = if (order.deliveryOtp.isNotBlank()) order.deliveryOtp else "----",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else if (order.paymentMethod == "BANK_TRANSFER") {
                // Bank Transfer Details Card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Virement Bancaire Local CEMAC",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (order.bankAccountName.isNotBlank()) {
                            Text(
                                text = "Banque : ${order.bankAccountName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (order.bankTransferRef.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Réf. Transaction : ${order.bankTransferRef}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (order.paymentProvider != null && order.paymentProvider.isNotBlank()) {
                // Mobile Money / Other
                val isOM = order.paymentProvider.contains("Orange", ignoreCase = true)
                Surface(
                    color = if (isOM) Color(0xFFFF7900).copy(alpha = 0.12f) else Color(0xFFFFCC00).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isOM) "🟠 Orange Money Cameroun" else "🟡 MTN Mobile Money",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isOM) Color(0xFFFF7900) else Color(0xFFC69200)
                        )
                        if (order.transactionRef != null && order.transactionRef.isNotBlank()) {
                            Text(
                                text = "TXN ID : ${order.transactionRef}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Items Purchased Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Détail de la commande :",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = if (order.itemsSummary.isNotBlank()) order.itemsSummary else "Articles divers commandés",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Destination & Customer Info
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Livraison : ${order.deliveryCity} - ${order.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (order.contactPhone.isNotBlank() || order.customerName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${order.customerName}${if (order.contactPhone.isNotBlank()) " • ${order.contactPhone}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Total Amount and Expansion Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Montant Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%,.0f".format(order.totalAmount)} XAF",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_details_${order.id}")
                ) {
                    Text(if (isExpanded) "Moins d'infos" else "Suivi détaillé")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Order Progress Steps
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Progression de la livraison :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OrderProgressStep(
                        title = "Commande enregistrée",
                        description = "Enregistrée sur le cloud Firestore",
                        isCompleted = true,
                        isCurrent = order.status == OrderStatus.PENDING
                    )

                    OrderProgressStep(
                        title = "Validation marchand",
                        description = "Préparation et emballage des articles",
                        isCompleted = order.status in listOf(
                            OrderStatus.PAID,
                            OrderStatus.PROCESSING,
                            OrderStatus.CONFIRMED,
                            OrderStatus.IN_TRANSIT,
                            OrderStatus.DELIVERED
                        ),
                        isCurrent = order.status in listOf(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.CONFIRMED)
                    )

                    OrderProgressStep(
                        title = "Prise en charge par le livreur",
                        description = "Expédié vers ${order.deliveryCity}",
                        isCompleted = order.status in listOf(OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED),
                        isCurrent = order.status == OrderStatus.IN_TRANSIT
                    )

                    OrderProgressStep(
                        title = "Colis remis au client",
                        description = "Vérification OTP et encaissement sécurisé",
                        isCompleted = order.status == OrderStatus.DELIVERED,
                        isCurrent = false
                    )
                }
            }
        }
    }
}

@Composable
fun OrderProgressStep(
    title: String,
    description: String,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isCurrent) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onTertiary)
                )
            }
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCompleted || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyOrdersView(
    title: String,
    message: String,
    onActionClick: () -> Unit,
    actionText: String = "Explorer la boutique"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onActionClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_state_action_button")
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(actionText)
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// Legacy compatibility models for other views in the same package (e.g. OrderTrackingScreen)
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
    val items: List<FirestoreOrderItem> = emptyList(),
    val paymentProvider: String = "Mobile Money",
    val paymentTransactionId: String = ""
)
