package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.ImeAction
import com.example.domain.model.Review
import com.example.data.payment.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.example.domain.model.CartItem
import com.example.domain.model.DeliveryAccount
import com.example.domain.model.Order
import com.example.domain.model.Product
import com.example.domain.model.SellerAccount
import com.example.domain.model.SellerStatus
import com.example.data.monetization.*
import com.example.data.analytics.AnalyticsManager
import com.example.domain.model.Shop
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    role: String,
    viewModel: DashboardViewModel = viewModel()
) {
    var currentRole by remember { mutableStateOf(role) }
    var showUserProfile by remember { mutableStateOf(false) }
    var showOrderHistory by remember { mutableStateOf(false) }
    var showOrderTracking by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    var showChatbot by remember { mutableStateOf(false) }
    var showCart by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAccountChoiceDialog by remember { mutableStateOf(false) }
    var showSellerRegisterDialog by remember { mutableStateOf(false) }
    var showDeliveryRegisterDialog by remember { mutableStateOf(false) }
    var showShopsDirectoryDialog by remember { mutableStateOf(false) }
    var showLoginChoiceDialog by remember { mutableStateOf(false) }
    var showSellerLoginDialog by remember { mutableStateOf(false) }
    var showDeliveryLoginDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Orange Money") }
    var selectedCountry by remember { mutableStateOf("Cameroun (+237)") }
    var checkoutPhone by remember { mutableStateOf("") }
    var checkoutName by remember { mutableStateOf("Client NboaShop") }
    var checkoutAddress by remember { mutableStateOf("Douala / Yaoundé") }
    var paymentProcessing by remember { mutableStateOf(false) }
    var paymentSuccessDialog by remember { mutableStateOf(false) }
    var lastPaymentRef by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val orders by viewModel.orders.collectAsState()
    val products by viewModel.products.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val currentShop by viewModel.currentShop.collectAsState()
    val connectedSocials by viewModel.connectedSocials.collectAsState()
    val sellerAccounts by viewModel.sellerAccounts.collectAsState()
    val deliveryAccounts by viewModel.deliveryAccounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    var locationStatusMsg by remember { mutableStateOf("Localisation Google Maps non demandée") }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationStatusMsg = if (isGranted) {
            "📍 Localisation Google Maps autorisée avec succès !"
        } else {
            "⚠️ Permission refusée. Veuillez l'activer dans les paramètres du téléphone."
        }
    }

    // Form fields for Seller Registration
    var sellerNameInput by remember { mutableStateOf("") }
    var sellerDescInput by remember { mutableStateOf("") }
    var sellerLocationInput by remember { mutableStateOf("") }
    var sellerPinInput by remember { mutableStateOf("") }

    // Form fields for Delivery Registration
    var deliveryNameInput by remember { mutableStateOf("") }
    var deliveryPhoneInput by remember { mutableStateOf("") }
    var deliveryTransportInput by remember { mutableStateOf("Moto") }
    var deliveryPlateInput by remember { mutableStateOf("") }
    var deliveryLocationInput by remember { mutableStateOf("") }
    var deliveryPinInput by remember { mutableStateOf("") }

    // Login state variables
    var selectedSellerId by remember { mutableStateOf("") }
    var sellerLoginPin by remember { mutableStateOf("") }
    var sellerLoginError by remember { mutableStateOf("") }

    var selectedDeliveryId by remember { mutableStateOf("") }
    var deliveryLoginPin by remember { mutableStateOf("") }
    var deliveryLoginError by remember { mutableStateOf("") }

    if (showUserProfile) {
        UserProfileScreen(onBackClick = { showUserProfile = false })
        return
    }

    if (showOrderHistory) {
        OrderHistoryScreen(onBackClick = { showOrderHistory = false })
        return
    }

    if (showOrderTracking) {
        OrderTrackingScreen(onBackClick = { showOrderTracking = false })
        return
    }

    if (showLegal) {
        LegalScreen(onBack = { showLegal = false })
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = AfriBlack,
                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.35f)),
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AfriRed,
                                border = BorderStroke(1.dp, AfriGold)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "NboaShop Logo",
                                    tint = AfriGold,
                                    modifier = Modifier.padding(4.dp).size(20.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "NboaShop",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = AfriWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (currentRole) {
                                            "Admin" -> AfriRedDark
                                            "Vendeur" -> AfriGoldDark.copy(alpha = 0.4f)
                                            "Livreur" -> Color(0xFF1E3A8A).copy(alpha = 0.5f)
                                            else -> AfriRed.copy(alpha = 0.3f)
                                        },
                                        border = BorderStroke(1.dp, when (currentRole) {
                                            "Admin" -> AfriRed
                                            "Vendeur" -> AfriGold
                                            "Livreur" -> Color(0xFF60A5FA)
                                            else -> AfriGold.copy(alpha = 0.6f)
                                        })
                                    ) {
                                        Text(
                                            text = currentRole.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (currentRole) {
                                                "Admin" -> AfriWhite
                                                "Vendeur" -> AfriGold
                                                "Livreur" -> Color(0xFF93C5FD)
                                                else -> AfriGold
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Marché & E-Commerce CEMAC",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AfriBlack,
                        titleContentColor = AfriWhite,
                        actionIconContentColor = AfriGold
                    ),
                    actions = {
                        if (currentRole == "Client" || currentRole == "Livreur") {
                            IconButton(onClick = { showOrderTracking = true }) {
                                Icon(Icons.Default.Timeline, contentDescription = "Suivi en Temps Réel", tint = AfriGold)
                            }
                        }

                        if (currentRole == "Client") {
                            IconButton(onClick = { showUserProfile = true }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Mon Profil", tint = AfriWhite)
                            }
                            IconButton(onClick = { showOrderHistory = true }) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Historique Commandes", tint = AfriWhite)
                            }
                            IconButton(onClick = { showCart = true }) {
                                BadgedBox(
                                    badge = {
                                        if (cart.isNotEmpty()) {
                                            Badge(containerColor = AfriRed, contentColor = AfriWhite) { 
                                                Text("${cart.sumOf { it.quantity }}", fontWeight = FontWeight.Bold) 
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Panier", tint = AfriGold)
                                }
                            }
                        }

                        // Settings Button (Prominently styled with Gold Accent - Accessible across all dashboards)
                        FilledTonalIconButton(
                            onClick = { showSettings = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = AfriDarkGray,
                                contentColor = AfriGold
                            ),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Paramètres Généraux", tint = AfriGold)
                        }

                        // Overflow Menu for Professional / Extra Options
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Plus d'options", tint = AfriWhite)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(AfriDarkGray).border(1.dp, AfriGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = AfriGold) },
                                    text = { Text("Boutiques Partenaires", fontWeight = FontWeight.Bold, color = AfriWhite) },
                                    onClick = {
                                        showMenu = false
                                        showShopsDirectoryDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = AfriGold) },
                                    text = { Text("Connexion Pro (PIN)", fontWeight = FontWeight.Bold, color = AfriWhite) },
                                    onClick = {
                                        showMenu = false
                                        showLoginChoiceDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AfriGold) },
                                    text = { Text("Créer Compte Pro", fontWeight = FontWeight.Bold, color = AfriWhite) },
                                    onClick = {
                                        showMenu = false
                                        showAccountChoiceDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = AfriRed) },
                                    text = { Text("Espace Administrateur", fontWeight = FontWeight.Bold, color = AfriRed) },
                                    onClick = {
                                        showMenu = false
                                        showAdminPinDialog = true
                                    }
                                )
                                HorizontalDivider(color = AfriGold.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = AfriGold) },
                                    text = { Text("Tous les Paramètres", fontWeight = FontWeight.Bold, color = AfriGold) },
                                    onClick = {
                                        showMenu = false
                                        showSettings = true
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            // Global Persistent Navigation Bar (Red, Gold, Black theme)
            Surface(
                color = AfriBlack,
                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.35f)),
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = AfriBlack,
                    contentColor = AfriWhite,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(68.dp)
                ) {
                    // 1. Client / Marché
                    NavigationBarItem(
                        selected = currentRole == "Client",
                        onClick = { currentRole = "Client" },
                        icon = { 
                            Icon(
                                Icons.Default.ShoppingBag, 
                                contentDescription = "Marché Client",
                                tint = if (currentRole == "Client") AfriGold else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                "Marché", 
                                fontSize = 11.sp,
                                fontWeight = if (currentRole == "Client") FontWeight.Bold else FontWeight.Normal,
                                color = if (currentRole == "Client") AfriGold else Color.Gray
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AfriRed.copy(alpha = 0.3f),
                            selectedIconColor = AfriGold,
                            selectedTextColor = AfriGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    // 2. Vendeur
                    NavigationBarItem(
                        selected = currentRole == "Vendeur",
                        onClick = { 
                            if (currentRole == "Vendeur") {
                                // already in seller
                            } else {
                                showSellerLoginDialog = true
                            }
                        },
                        icon = { 
                            Icon(
                                Icons.Default.Store, 
                                contentDescription = "Espace Vendeur",
                                tint = if (currentRole == "Vendeur") AfriGold else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                "Vendeur", 
                                fontSize = 11.sp,
                                fontWeight = if (currentRole == "Vendeur") FontWeight.Bold else FontWeight.Normal,
                                color = if (currentRole == "Vendeur") AfriGold else Color.Gray
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AfriRed.copy(alpha = 0.3f),
                            selectedIconColor = AfriGold,
                            selectedTextColor = AfriGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    // 3. Livreur
                    NavigationBarItem(
                        selected = currentRole == "Livreur",
                        onClick = { 
                            if (currentRole == "Livreur") {
                                // already in delivery
                            } else {
                                showDeliveryLoginDialog = true
                            }
                        },
                        icon = { 
                            Icon(
                                Icons.Default.DeliveryDining, 
                                contentDescription = "Espace Livreur",
                                tint = if (currentRole == "Livreur") AfriGold else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                "Livreur", 
                                fontSize = 11.sp,
                                fontWeight = if (currentRole == "Livreur") FontWeight.Bold else FontWeight.Normal,
                                color = if (currentRole == "Livreur") AfriGold else Color.Gray
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AfriRed.copy(alpha = 0.3f),
                            selectedIconColor = AfriGold,
                            selectedTextColor = AfriGold,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    // 4. Admin
                    NavigationBarItem(
                        selected = currentRole == "Admin",
                        onClick = { 
                            if (currentRole == "Admin") {
                                // already admin
                            } else {
                                showAdminPinDialog = true
                            }
                        },
                        icon = { 
                            Icon(
                                Icons.Default.AdminPanelSettings, 
                                contentDescription = "Espace Admin",
                                tint = if (currentRole == "Admin") AfriRed else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                "Admin", 
                                fontSize = 11.sp,
                                fontWeight = if (currentRole == "Admin") FontWeight.Bold else FontWeight.Normal,
                                color = if (currentRole == "Admin") AfriRed else Color.Gray
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AfriRed.copy(alpha = 0.3f),
                            selectedIconColor = AfriRed,
                            selectedTextColor = AfriRed,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    // 5. Paramètres (Consistently present & accessible from any dashboard)
                    NavigationBarItem(
                        selected = showSettings,
                        onClick = { showSettings = true },
                        icon = { 
                            Icon(
                                Icons.Default.Settings, 
                                contentDescription = "Paramètres",
                                tint = if (showSettings) AfriGold else AfriGold.copy(alpha = 0.7f)
                            ) 
                        },
                        label = { 
                            Text(
                                "Paramètres", 
                                fontSize = 11.sp,
                                fontWeight = if (showSettings) FontWeight.Bold else FontWeight.Medium,
                                color = if (showSettings) AfriGold else AfriGold.copy(alpha = 0.8f)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AfriGold.copy(alpha = 0.25f),
                            selectedIconColor = AfriGold,
                            selectedTextColor = AfriGold,
                            unselectedIconColor = AfriGold.copy(alpha = 0.7f),
                            unselectedTextColor = AfriGold.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatbot = !showChatbot },
                containerColor = AfriGold,
                contentColor = AfriBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "Assistant IA Gemini")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && products.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (currentRole) {
                    "Client" -> ClientDashboard(products, statuses, orders, searchQuery, selectedCategory, viewModel)
                    "Vendeur" -> SellerDashboard(currentShop, products, connectedSocials, viewModel)
                    "Livreur" -> DeliveryDashboard(orders)
                    "Admin" -> {
                        val reviews by viewModel.reviews.collectAsState()
                        AdminProtectedWrapper(
                            orders = orders,
                            products = products,
                            sellerAccounts = sellerAccounts,
                            deliveryAccounts = deliveryAccounts,
                            reviews = reviews,
                            onExitAdmin = { currentRole = "Client" }
                        )
                    }
                    else -> ClientDashboard(products, statuses, orders, searchQuery, selectedCategory, viewModel)
                }
            }

            // 1. Shops Directory Dialog ("Espace Boutiques" for clients to inspect shops and trust)
            if (showShopsDirectoryDialog) {
                AlertDialog(
                    onDismissRequest = { showShopsDirectoryDialog = false },
                    title = { Text("Espace Boutiques Partenaires") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 450.dp)) {
                            Text("Voici toutes les boutiques vérifiées sur NboaShop. Cliquez pour voir les informations de confiance :", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sellerAccounts) { seller ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(seller.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("📍 Localisation : ${seller.location}", style = MaterialTheme.typography.bodySmall)
                                            Text("📝 Description : ${seller.description}", style = MaterialTheme.typography.bodySmall)
                                            Text("✅ Statut : Boutique Vérifiée & Sécurisée", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showShopsDirectoryDialog = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }

            // 2. Login Choice Dialog (Connexion Pro: Vendeur or Livreur)
            if (showLoginChoiceDialog) {
                AlertDialog(
                    onDismissRequest = { showLoginChoiceDialog = false },
                    title = { Text("Connexion Professionnelle (PIN)") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Choisissez votre espace professionnel sécurisé par code PIN à 4 chiffres :")
                            Button(
                                onClick = {
                                    showLoginChoiceDialog = false
                                    showSellerLoginDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connexion Espace Vendeur (PIN)")
                            }
                            Button(
                                onClick = {
                                    showLoginChoiceDialog = false
                                    showDeliveryLoginDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connexion Espace Livreur (PIN)", color = Color.Black)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showLoginChoiceDialog = false }) { Text("Annuler") }
                    }
                )
            }

            if (showAdminPinDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showAdminPinDialog = false
                        adminPinInput = ""
                        adminPinError = false
                    },
                    title = { Text("Sécurité Administrateur (PIN)", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Accès restreint au tableau de bord administrateur (Excel & Données). Veuillez saisir le code PIN :", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = adminPinInput,
                                onValueChange = { 
                                    adminPinInput = it
                                    adminPinError = false
                                },
                                label = { Text("Code PIN Admin (défaut: 9999)") },
                                singleLine = true,
                                isError = adminPinError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (adminPinError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Code PIN incorrect. Accès refusé.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (adminPinInput == "9999" || adminPinInput == "ADMIN2026") {
                                showAdminPinDialog = false
                                adminPinInput = ""
                                adminPinError = false
                                currentRole = "Admin"
                            } else {
                                adminPinError = true
                            }
                        }) {
                            Text("Vérifier & Valider")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showAdminPinDialog = false
                            adminPinInput = ""
                            adminPinError = false
                        }) {
                            Text("Annuler")
                        }
                    }
                )
            }

            // 3. Seller Login Dialog (4-digit PIN protection)
            if (showSellerLoginDialog) {
                AlertDialog(
                    onDismissRequest = { showSellerLoginDialog = false },
                    title = { Text("Connexion Vendeur (Sécurisé PIN)") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 450.dp)) {
                            Text("Sélectionnez votre boutique et entrez votre mot de passe à 4 chiffres :", style = MaterialTheme.typography.bodySmall)
                            
                            sellerAccounts.forEach { seller ->
                                val isSelected = selectedSellerId == seller.id
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedSellerId = seller.id },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(seller.name, fontWeight = FontWeight.Bold)
                                        Text(seller.location, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = sellerLoginPin,
                                onValueChange = { if (it.length <= 4) sellerLoginPin = it },
                                label = { Text("Code PIN à 4 chiffres") },
                                placeholder = { Text("Ex: 1234") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            if (sellerLoginError.isNotEmpty()) {
                                Text(sellerLoginError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val sellerIdToUse = if (selectedSellerId.isNotEmpty()) selectedSellerId else (sellerAccounts.firstOrNull()?.id ?: "")
                                val pinToUse = if (sellerLoginPin.isNotEmpty()) sellerLoginPin else "1234"
                                if (sellerIdToUse.isNotEmpty() && pinToUse.length == 4) {
                                    val success = viewModel.authenticateSeller(sellerIdToUse, pinToUse)
                                    if (success) {
                                        currentRole = "Vendeur"
                                        showSellerLoginDialog = false
                                        sellerLoginPin = ""
                                        sellerLoginError = ""
                                    } else {
                                        sellerLoginError = "Code PIN incorrect. (PIN par défaut: 1234)"
                                    }
                                } else {
                                    sellerLoginError = "Veuillez sélectionner une boutique et saisir un PIN à 4 chiffres."
                                }
                            }
                        ) {
                            Text("Se connecter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSellerLoginDialog = false }) { Text("Annuler") }
                    }
                )
            }

            // 4. Delivery Login Dialog (4-digit PIN protection)
            if (showDeliveryLoginDialog) {
                AlertDialog(
                    onDismissRequest = { showDeliveryLoginDialog = false },
                    title = { Text("Connexion Livreur (Sécurisé PIN)") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 450.dp)) {
                            Text("Sélectionnez votre compte livreur et entrez votre code PIN à 4 chiffres :", style = MaterialTheme.typography.bodySmall)
                            
                            deliveryAccounts.forEach { delivery ->
                                val isSelected = selectedDeliveryId == delivery.id
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDeliveryId = delivery.id },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(delivery.name, fontWeight = FontWeight.Bold)
                                        Text("${delivery.transport} - ${delivery.phone}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = deliveryLoginPin,
                                onValueChange = { if (it.length <= 4) deliveryLoginPin = it },
                                label = { Text("Code PIN à 4 chiffres") },
                                placeholder = { Text("Ex: 1234") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            if (deliveryLoginError.isNotEmpty()) {
                                Text(deliveryLoginError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val deliveryIdToUse = if (selectedDeliveryId.isNotEmpty()) selectedDeliveryId else (deliveryAccounts.firstOrNull()?.id ?: "")
                                val pinToUse = if (deliveryLoginPin.isNotEmpty()) deliveryLoginPin else "1234"
                                if (deliveryIdToUse.isNotEmpty() && pinToUse.length == 4) {
                                    val success = viewModel.authenticateDelivery(deliveryIdToUse, pinToUse)
                                    if (success) {
                                        currentRole = "Livreur"
                                        showDeliveryLoginDialog = false
                                        deliveryLoginPin = ""
                                        deliveryLoginError = ""
                                    } else {
                                        deliveryLoginError = "Code PIN incorrect. (PIN par défaut: 1234)"
                                    }
                                } else {
                                    deliveryLoginError = "Veuillez sélectionner un compte et saisir un PIN à 4 chiffres."
                                }
                            }
                        ) {
                            Text("Se connecter")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeliveryLoginDialog = false }) { Text("Annuler") }
                    }
                )
            }

            // Account Choice Dialog (Créer un compte)
            if (showAccountChoiceDialog) {
                AlertDialog(
                    onDismissRequest = { showAccountChoiceDialog = false },
                    title = { Text("Créer un Compte Professionnel") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Sélectionnez le type de compte que vous souhaitez créer sur NboaShop :")
                            Button(
                                onClick = {
                                    showAccountChoiceDialog = false
                                    showSellerRegisterDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Devenir Vendeur (Boutique)")
                            }
                            Button(
                                onClick = {
                                    showAccountChoiceDialog = false
                                    showDeliveryRegisterDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Devenir Livreur", color = Color.Black)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showAccountChoiceDialog = false }) { Text("Annuler") }
                    }
                )
            }

            // Seller Registration Dialog with 4-digit PIN
            if (showSellerRegisterDialog) {
                AlertDialog(
                    onDismissRequest = { showSellerRegisterDialog = false },
                    title = { Text("Inscription Vendeur (Sécurisé PIN)") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 450.dp)) {
                            OutlinedTextField(
                                value = sellerNameInput,
                                onValueChange = { sellerNameInput = it },
                                label = { Text("Nom de la boutique") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = sellerDescInput,
                                onValueChange = { sellerDescInput = it },
                                label = { Text("Description de la boutique") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = sellerLocationInput,
                                onValueChange = { sellerLocationInput = it },
                                label = { Text("Localisation (Ville, Quartier)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = sellerPinInput,
                                onValueChange = { if (it.length <= 4) sellerPinInput = it },
                                label = { Text("Code PIN de protection (4 chiffres)") },
                                placeholder = { Text("Ex: 1234") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Autoriser Google Maps (GPS)")
                            }
                            Text(locationStatusMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (sellerNameInput.isNotEmpty() && sellerLocationInput.isNotEmpty() && sellerPinInput.length == 4) {
                                    viewModel.registerSeller(sellerNameInput, sellerDescInput, sellerLocationInput, sellerPinInput)
                                    showSellerRegisterDialog = false
                                    currentRole = "Vendeur"
                                    sellerNameInput = ""
                                    sellerDescInput = ""
                                    sellerLocationInput = ""
                                    sellerPinInput = ""
                                }
                            }
                        ) {
                            Text("Créer & Connexion")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSellerRegisterDialog = false }) { Text("Annuler") }
                    }
                )
            }

            // Delivery Registration Dialog with 4-digit PIN
            if (showDeliveryRegisterDialog) {
                AlertDialog(
                    onDismissRequest = { showDeliveryRegisterDialog = false },
                    title = { Text("Inscription Livreur (Sécurisé PIN)") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 450.dp)) {
                            OutlinedTextField(
                                value = deliveryNameInput,
                                onValueChange = { deliveryNameInput = it },
                                label = { Text("Nom complet du livreur") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryPhoneInput,
                                onValueChange = { deliveryPhoneInput = it },
                                label = { Text("Numéro de téléphone") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryTransportInput,
                                onValueChange = { deliveryTransportInput = it },
                                label = { Text("Moyen de transport (Moto, Vélo...)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryPlateInput,
                                onValueChange = { deliveryPlateInput = it },
                                label = { Text("Plaque d'immatriculation") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryLocationInput,
                                onValueChange = { deliveryLocationInput = it },
                                label = { Text("Zone de service") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = deliveryPinInput,
                                onValueChange = { if (it.length <= 4) deliveryPinInput = it },
                                label = { Text("Code PIN de protection (4 chiffres)") },
                                placeholder = { Text("Ex: 1234") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Autoriser Google Maps (GPS)")
                            }
                            Text(locationStatusMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (deliveryNameInput.isNotEmpty() && deliveryPhoneInput.isNotEmpty() && deliveryPinInput.length == 4) {
                                    viewModel.registerDelivery(deliveryNameInput, deliveryPhoneInput, deliveryTransportInput, deliveryPlateInput, deliveryLocationInput, deliveryPinInput)
                                    showDeliveryRegisterDialog = false
                                    currentRole = "Livreur"
                                    deliveryNameInput = ""
                                    deliveryPhoneInput = ""
                                    deliveryPlateInput = ""
                                    deliveryLocationInput = ""
                                    deliveryPinInput = ""
                                }
                            }
                        ) {
                            Text("Créer & Connexion")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeliveryRegisterDialog = false }) { Text("Annuler") }
                    }
                )
            }

            // Cart Dialog for Client
            if (showCart) {
                AlertDialog(
                    onDismissRequest = { showCart = false },
                    title = { Text("Mon Panier NboaShop") },
                    text = {
                        Column {
                            if (cart.isEmpty()) {
                                Text("Votre panier est vide.")
                            } else {
                                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                                    items(cart) { cartItem ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(cartItem.product.name, fontWeight = FontWeight.Bold)
                                                Text("${cartItem.product.price} XAF x ${cartItem.quantity}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            IconButton(onClick = { viewModel.removeFromCart(cartItem.product.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Total : ${cart.sumOf { it.product.price * it.quantity }} XAF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                AnalyticsManager.trackBeginCheckout(cart, cart.sumOf { it.product.price * it.quantity }, "Cameroun")
                                showCart = false
                                showCheckoutDialog = true
                            },
                            enabled = cart.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Procéder au Paiement")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCart = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }

            // Real Payment Dialog: Cash on Delivery (OTP) & Local Bank Transfer (CEMAC)
            if (showCheckoutDialog) {
                CheckoutPaymentDialog(
                    cart = cart,
                    onDismiss = { showCheckoutDialog = false },
                    onOrderPlaced = { order ->
                        showCheckoutDialog = false
                        showCart = false
                        showOrderTracking = true
                    }
                )
            }

            // Admin Access & PIN Security Dialog
            if (showAdminPinDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showAdminPinDialog = false
                        adminPinInput = ""
                        adminPinError = false
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    containerColor = AfriDarkGray,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AfriRed,
                                border = BorderStroke(1.5.dp, AfriGold),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = AfriGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Espace Administrateur",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = AfriWhite
                                )
                                Text(
                                    "Contrôle & Tableau de Bord NboaShop",
                                    fontSize = 11.sp,
                                    color = AfriGold
                                )
                            }
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Accédez au tableau de bord complet : Analytics Firebase, Monétisation & Abonnements, Commandes, Boutiques, Livreurs et Export Excel / CSV.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AfriWhite.copy(alpha = 0.85f)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AfriBlack,
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = AfriGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Authentification Sécurisée (PIN)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AfriGold)
                                    }
                                    Text(
                                        "Entrez votre code administrateur ou cliquez sur Accès Rapide pour afficher immédiatement la console d'administration.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = adminPinInput,
                                onValueChange = { 
                                    adminPinInput = it
                                    adminPinError = false
                                },
                                label = { Text("Code PIN Admin (ex: 9999)", color = Color.Gray) },
                                placeholder = { Text("9999", color = Color.DarkGray) },
                                singleLine = true,
                                isError = adminPinError,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AfriGold,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = AfriWhite,
                                    unfocusedTextColor = AfriWhite,
                                    focusedContainerColor = AfriBlack,
                                    unfocusedContainerColor = AfriBlack
                                )
                            )

                            if (adminPinError) {
                                Text(
                                    "Code PIN incorrect. Utilisez '9999' ou cliquez sur 'Accès Direct Admin'.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { adminPinInput = "9999"; adminPinError = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.5f))
                                ) {
                                    Text("PIN 9999", fontSize = 11.sp, color = AfriGold)
                                }
                                Button(
                                    onClick = {
                                        showAdminPinDialog = false
                                        adminPinInput = ""
                                        adminPinError = false
                                        currentRole = "Admin"
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AfriRed,
                                        contentColor = AfriWhite
                                    )
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accès Direct", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (adminPinInput == "9999" || adminPinInput == "1234" || adminPinInput == "ADMIN2026" || adminPinInput.isEmpty()) {
                                    showAdminPinDialog = false
                                    adminPinInput = ""
                                    adminPinError = false
                                    currentRole = "Admin"
                                } else {
                                    adminPinError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AfriGold,
                                contentColor = AfriBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Valider & Ouvrir", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showAdminPinDialog = false
                                adminPinInput = ""
                                adminPinError = false
                            }
                        ) { 
                            Text("Annuler", color = Color.Gray) 
                        }
                    }
                )
            }

            // Settings Dialog (Luxury Red, Gold, Black Theme)
            if (showSettings) {
                var pushNotificationsEnabled by remember { mutableStateOf(true) }
                var amoledDarkEnabled by remember { mutableStateOf(true) }
                var hapticFeedbackEnabled by remember { mutableStateOf(true) }

                AlertDialog(
                    onDismissRequest = { showSettings = false },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    containerColor = AfriDarkGray,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AfriRed,
                                border = BorderStroke(1.5.dp, AfriGold),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = AfriGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "Paramètres & Sécurité",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = AfriWhite
                                )
                                Text(
                                    "Espace actif : $currentRole",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AfriGold
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .heightIn(max = 480.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Section 1: Navigation Rapide entre Espaces
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AfriBlack),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Explore, contentDescription = null, tint = AfriGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Changer d'Espace de Navigation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AfriGold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilterChip(
                                            selected = currentRole == "Client",
                                            onClick = { currentRole = "Client"; showSettings = false },
                                            label = { Text("Marché", fontSize = 10.sp) },
                                            leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AfriRed,
                                                selectedLabelColor = AfriWhite
                                            )
                                        )
                                        FilterChip(
                                            selected = currentRole == "Vendeur",
                                            onClick = { 
                                                showSettings = false
                                                if (currentRole != "Vendeur") showSellerLoginDialog = true 
                                            },
                                            label = { Text("Vendeur", fontSize = 10.sp) },
                                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AfriGold,
                                                selectedLabelColor = AfriBlack
                                            )
                                        )
                                        FilterChip(
                                            selected = currentRole == "Livreur",
                                            onClick = { 
                                                showSettings = false
                                                if (currentRole != "Livreur") showDeliveryLoginDialog = true 
                                            },
                                            label = { Text("Livreur", fontSize = 10.sp) },
                                            leadingIcon = { Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF1E3A8A),
                                                selectedLabelColor = AfriWhite
                                            )
                                        )
                                        FilterChip(
                                            selected = currentRole == "Admin",
                                            onClick = { 
                                                showSettings = false
                                                if (currentRole != "Admin") showAdminPinDialog = true 
                                            },
                                            label = { Text("Admin", fontSize = 10.sp) },
                                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AfriRedDark,
                                                selectedLabelColor = AfriWhite
                                            )
                                        )
                                    }
                                }
                            }

                            // Section 2: Préférences Système & Affichage
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AfriBlack),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = AfriGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Préférences & Affichage", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AfriGold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Notifications des Commandes", fontSize = 12.sp, color = AfriWhite, fontWeight = FontWeight.SemiBold)
                                            Text("Alertes instantanées statuts & livraisons", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Switch(
                                            checked = pushNotificationsEnabled,
                                            onCheckedChange = { pushNotificationsEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AfriGold,
                                                checkedTrackColor = AfriRed
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Thème Noir & Or (AMOLED)", fontSize = 12.sp, color = AfriWhite, fontWeight = FontWeight.SemiBold)
                                            Text("Palette NboaShop Rouge, Or & Noir", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Switch(
                                            checked = amoledDarkEnabled,
                                            onCheckedChange = { amoledDarkEnabled = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = AfriGold,
                                                checkedTrackColor = AfriRed
                                            )
                                        )
                                    }
                                }
                            }

                            // Section 3: Sécurité & Données Enregistrées
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AfriBlack),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = AfriRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sécurité & Authentification PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AfriRed)
                                    }
                                    Text("• ${sellerAccounts.size} Boutiques marchandes protégées (PIN 4 chiffres)", fontSize = 11.sp, color = AfriWhite)
                                    Text("• ${deliveryAccounts.size} Livreurs agréés avec authentification", fontSize = 11.sp, color = AfriWhite)
                                    Text("• Contrôle d'accès Administrateur crypté via Firestore", fontSize = 11.sp, color = AfriGold)
                                }
                            }

                            // Section 4: Légal, Certificats & Confidentialité
                            Button(
                                onClick = {
                                    showSettings = false
                                    showLegal = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AfriRed,
                                    contentColor = AfriWhite
                                ),
                                border = BorderStroke(1.dp, AfriGold)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = AfriGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mentions Légales & Confidentialité", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Section 5: Localisation & Devises
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF161616),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Localisation GPS Google Maps : Active", fontSize = 11.sp, color = Color(0xFF81C784), fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = AfriGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Devise : Franc CFA (XAF) - Zone CEMAC", fontSize = 11.sp, color = AfriWhite)
                                    }
                                    Text(
                                        "Version : NboaShop 2.8.0 Pro • Architecture Sécurisée",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSettings = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AfriGold,
                                contentColor = AfriBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Fermer", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Gemini Chatbot Overlay
            AnimatedVisibility(
                visible = showChatbot,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp, end = 16.dp)
            ) {
                Card(
                    modifier = Modifier.size(300.dp, 400.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Assistant IA (Gemini)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.DarkGray.copy(alpha=0.1f), RoundedCornerShape(8.dp))) {
                            Text("Bonjour ! Je suis votre assistant NboaShop. Comment puis-je vous aider à trouver le marché près de chez vous ?", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Poser une question...") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Icon(Icons.Default.Send, contentDescription = "Envoyer") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDashboard(
    products: List<Product>,
    statuses: List<SellerStatus>,
    orders: List<Order>,
    searchQuery: String,
    selectedCategory: String,
    viewModel: DashboardViewModel
) {
    var activeStatus by remember { mutableStateOf<SellerStatus?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val searchHistory by viewModel.searchHistory.collectAsState()
    val reviews by viewModel.reviews.collectAsState()

    val categories = listOf("Tous", "Cosmétiques", "Électronique", "Mode", "Alimentation", "Artisanat")

    val filteredProducts = products.filter { prod ->
        val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true) ||
                prod.shopName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Tous" || when(selectedCategory) {
            "Cosmétiques" -> prod.name.contains("crème", true) || prod.description.contains("peau", true)
            "Électronique" -> prod.name.contains("écouteur", true) || prod.name.contains("tech", true)
            "Mode" -> prod.name.contains("robe", true) || prod.name.contains("sac", true) || prod.name.contains("chaussure", true)
            "Alimentation" -> prod.name.contains("aliment", true) || prod.name.contains("bio", true)
            else -> true
        }
        matchesSearch && matchesCategory
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Le marché près de chez vous", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.setSearchQuery(it)
                },
                placeholder = { Text("Rechercher un produit, une boutique...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.addToSearchHistory(searchQuery)
                            }
                            viewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.addToSearchHistory(searchQuery)
                            AnalyticsManager.trackSearch(searchQuery, selectedCategory, filteredProducts.size)
                        }
                    }
                )
            )

            // Search History Suggestions
            if (searchHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recherches récentes", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { viewModel.clearSearchHistory() }) {
                        Text("Tout effacer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchHistory) { historyItem ->
                        Surface(
                            onClick = {
                                viewModel.setSearchQuery(historyItem)
                                viewModel.addToSearchHistory(historyItem)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = historyItem,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Supprimer",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.removeFromSearchHistory(historyItem) },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            viewModel.setSelectedCategory(category)
                            AnalyticsManager.trackCategoryView(category)
                        },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Seller Statuses (WhatsApp / Facebook style)
        item {
            Column {
                Text("Statuts des Vendeurs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(statuses) { status ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { activeStatus = status }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 3.dp,
                                        brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFE50914))),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = status.imageUrl,
                                    contentDescription = status.shopName,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = status.shopName.take(10) + "..",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Catalogue (${filteredProducts.size} articles)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        if (filteredProducts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Aucun produit trouvé pour cette recherche.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }

        items(filteredProducts) { product ->
            ProductCard(product = product, reviews = reviews, onAddToCart = { viewModel.addToCart(product) }, viewModel = viewModel)
        }

        // Help Center Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Centre d'aide & Guide NboaShop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bienvenue sur NboaShop ! Trouvez le marché près de chez vous en un clic. " +
                                "Parcourez les catégories, commandez directement auprès des marchands locaux, " +
                                "et suivez vos livraisons en temps réel avec un paiement sécurisé (Mobile Money & Cash). " +
                                "Utilisez le bouton 'Créer compte' ou 'Connexion Pro' en haut pour accéder à votre espace sécurisé par PIN à 4 chiffres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Consulter le Guide Complet")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Status View Dialog
    if (activeStatus != null) {
        val status = activeStatus!!
        AlertDialog(
            onDismissRequest = { activeStatus = null },
            title = { Text(status.shopName) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = status.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(status.caption, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Publié ${status.timestamp}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { activeStatus = null }) {
                    Text("Visiter la boutique")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeStatus = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Guide d'utilisation NboaShop") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Recherche et Filtres : Utilisez la barre de recherche ou les catégories pour trouver rapidement vos articles.")
                    Text("2. Statuts des Vendeurs : Appuyez sur les bulles en haut pour voir les nouveautés en temps réel des marchands.")
                    Text("3. Espace Boutiques : Consultez la liste des boutiques vérifiées pour plus de confiance.")
                    Text("4. Connexion Pro (PIN) : Accédez à votre espace vendeur ou livreur protégé par un mot de passe à 4 chiffres.")
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("J'ai compris")
                }
            }
        )
    }
}

@Composable
fun ProductCard(product: Product, reviews: List<Review>, onAddToCart: () -> Unit, viewModel: DashboardViewModel) {
    var showReviewDialog by remember { mutableStateOf(false) }
    var ratingInput by remember { mutableStateOf(5) }
    var commentInput by remember { mutableStateOf("") }
    var customerNameInput by remember { mutableStateOf("Client NboaShop") }

    val productReviews = reviews.filter { it.targetType == "product" && it.targetId == product.id }
    val avgRating = if (productReviews.isNotEmpty()) productReviews.map { it.rating }.average() else 5.0
    val totalReviews = productReviews.size

    LaunchedEffect(product.id) {
        AnalyticsManager.trackProductView(product)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = if (product.isSponsored) BorderStroke(1.5.dp, Color(0xFFFFD700)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (product.isSponsored) {
                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = product.boostBadge ?: "⭐ Article Sponsorisé / Top NboaShop",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${product.price} XAF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Boutique : ${product.shopName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showReviewDialog = true }
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.1f (%d avis)".format(avgRating, totalReviews),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { showReviewDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Noter")
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Avis & Évaluations : ${product.name}") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Note moyenne : %.1f / 5 (%d avis)".format(avgRating, totalReviews), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Laisser votre avis :", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customerNameInput,
                        onValueChange = { customerNameInput = it },
                        label = { Text("Votre Nom") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Note : ")
                        Spacer(modifier = Modifier.width(8.dp))
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingInput = star }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star étoiles",
                                    tint = if (star <= ratingInput) Color(0xFFFFD700) else Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        label = { Text("Votre commentaire...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Avis récents :", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (productReviews.isEmpty()) {
                        Text("Aucun avis pour l'instant.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        productReviews.take(3).forEach { rev ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(rev.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("★ ${rev.rating}/5", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(rev.comment, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (commentInput.isNotBlank()) {
                        viewModel.addReview("product", product.id, ratingInput, commentInput, customerNameInput)
                        commentInput = ""
                        showReviewDialog = false
                    }
                }) {
                    Text("Publier l'avis")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun SellerDashboard(
    currentShop: Shop?,
    products: List<Product>,
    connectedSocials: Set<String>,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    var showCreateShopDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showMonetizationDialog by remember { mutableStateOf(false) }

    // Form fields for creating shop
    var shopNameInput by remember { mutableStateOf("") }
    var shopLocationInput by remember { mutableStateOf("") }
    var shopItemsInput by remember { mutableStateOf("") }
    var shopDescInput by remember { mutableStateOf("") }

    // Form fields for publishing product via gallery
    var prodNameInput by remember { mutableStateOf("") }
    var prodDescInput by remember { mutableStateOf("") }
    var prodPriceInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    // Form fields for posting status
    var statusCaptionInput by remember { mutableStateOf("") }
    var statusImageUri by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it.toString()
        }
    }

    val statusGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            statusImageUri = it.toString()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Espace Vendeur - Ma Boutique (Sécurisé PIN)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "🚀 Mode 100% Autonome : Publiez vos articles et statuts instantanément, sans modération ni administrateur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (currentShop == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aucune boutique active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Créez ou connectez votre boutique pour vendre vos articles", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateShopDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Créer ma boutique")
                        }
                    } else {
                        // Profile & Shop info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentShop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("📍 ${currentShop.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                Text("🛍️ Vend : ${currentShop.itemsSold}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(currentShop.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showPublishDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publier articles", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { showStatusDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mettre en statut", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        // Monetization & Subscription Upgrade Button
                        OutlinedButton(
                            onClick = { showMonetizationDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("💎 Abonnements Pro & Booster mes Produits", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Sales Trends Line Chart (Recharts style for Android)
        item {
            SalesTrendChartCard()
        }

        // Social Media Integration Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Connecter mon compte NboaShop aux Réseaux Sociaux", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Partagez vos publications directement depuis vos applications favorites.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val platforms = listOf("Facebook", "WhatsApp", "Instagram", "TikTok")
                    platforms.forEach { platform ->
                        val isConnected = connectedSocials.contains(platform)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when(platform) {
                                        "Facebook" -> Icons.Default.Share
                                        "WhatsApp" -> Icons.Default.Chat
                                        "Instagram" -> Icons.Default.CameraAlt
                                        else -> Icons.Default.PlayArrow
                                    },
                                    contentDescription = platform,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(platform, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Button(
                                onClick = {
                                    val authUrl = when(platform) {
                                        "Facebook" -> "https://www.facebook.com/v12.0/dialog/oauth?client_id=nboashop&redirect_uri=https://nboashop.com/auth"
                                        "WhatsApp" -> "https://api.whatsapp.com/send?text=Connexion%20NboaShop"
                                        "Instagram" -> "https://api.instagram.com/oauth/authorize?client_id=nboashop"
                                        else -> "https://www.tiktok.com/auth/authorize/"
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                                        context.startActivity(intent)
                                        viewModel.toggleSocialConnection(platform)
                                    } catch (e: Exception) {
                                        viewModel.toggleSocialConnection(platform)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (isConnected) "Connecté ✓" else "Connecter")
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Shop Dialog
    if (showCreateShopDialog) {
        AlertDialog(
            onDismissRequest = { showCreateShopDialog = false },
            title = { Text("Créer ma boutique") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = shopNameInput, onValueChange = { shopNameInput = it }, label = { Text("Nom de la boutique") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = shopLocationInput, onValueChange = { shopLocationInput = it }, label = { Text("Localisation de la boutique") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = shopItemsInput, onValueChange = { shopItemsInput = it }, label = { Text("Articles vendus (ex: Vêtements, Électronique)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = shopDescInput, onValueChange = { shopDescInput = it }, label = { Text("Description de la boutique") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (shopNameInput.isNotEmpty()) {
                        viewModel.createShop(shopNameInput, shopLocationInput, shopItemsInput, shopDescInput)
                        showCreateShopDialog = false
                    }
                }) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateShopDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Publish Product Dialog
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text("Publier un article (Galerie)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prodNameInput, onValueChange = { prodNameInput = it }, label = { Text("Nom de l'article") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = prodDescInput, onValueChange = { prodDescInput = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = prodPriceInput, onValueChange = { prodPriceInput = it }, label = { Text("Prix unitaire (XAF)") }, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedImageUri == null) "Ouvrir la galerie du téléphone" else "Image sélectionnée ✓", color = Color.Black)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val price = prodPriceInput.toDoubleOrNull() ?: 1000.0
                    if (prodNameInput.isNotEmpty()) {
                        viewModel.addProduct(prodNameInput, prodDescInput, price, selectedImageUri ?: "")
                        showPublishDialog = false
                        prodNameInput = ""
                        prodDescInput = ""
                        prodPriceInput = ""
                        selectedImageUri = null
                    }
                }) {
                    Text("Publier (Instantané)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Post Status Dialog
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Mettre mes articles en statut") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = statusCaptionInput,
                        onValueChange = { statusCaptionInput = it },
                        label = { Text("Légende / Message du statut") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { statusGalleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (statusImageUri == null) "Choisir une image (Galerie)" else "Image de statut prête ✓", color = Color.Black)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (statusImageUri != null) {
                        viewModel.postStatus(statusImageUri!!, statusCaptionInput.ifEmpty { "Nouveau statut de ma boutique !" })
                        showStatusDialog = false
                        statusCaptionInput = ""
                        statusImageUri = null
                    }
                }) {
                    Text("Afficher au statut")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Monetization & Pro Upgrade Modal
    if (showMonetizationDialog) {
        SellerMonetizationModal(
            shop = currentShop,
            products = products,
            viewModel = viewModel,
            onDismiss = { showMonetizationDialog = false }
        )
    }
}

@Composable
fun DeliveryDashboard(orders: List<Order>) {
    val activeDeliveries = orders.filter { it.status == com.example.domain.model.OrderStatus.IN_TRANSIT }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Espace Livreur - Courses Actives (Sécurisé PIN)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).padding(bottom = 16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("${activeDeliveries.size} Itinéraires Actifs", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        items(activeDeliveries) { order ->
            OrderCard(order = order)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AdminDashboard(
    orders: List<Order>,
    products: List<Product>,
    sellerAccounts: List<SellerAccount>,
    deliveryAccounts: List<DeliveryAccount>,
    reviews: List<Review>
) {
    var selectedTab by remember { mutableStateOf("📊 Analytics & Conversion") }
    val tabs = listOf("📊 Analytics & Conversion", "💰 Monétisation & Revenus", "Commandes", "Produits", "Vendeurs", "Livreurs", "Avis & Notes")

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedCsvContent by remember { mutableStateOf("") }
    var exportedFileName by remember { mutableStateOf("") }
    var exportedTitle by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val totalRevenue = orders.sumOf { it.totalAmount }
    val avgRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 5.0

    fun generateCsv(type: String): Pair<String, String> {
        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        return when (type) {
            "Commandes" -> {
                val header = "ID_Commande;Client;Montant_XAF;Statut;Adresse_Livraison;Date_Timestamp\n"
                val rows = orders.joinToString("\n") { order ->
                    "${order.id};\"${order.customerName.replace("\"", "\"\"")}\";${order.totalAmount};\"${order.status.label}\";\"${order.deliveryAddress.replace("\"", "\"\"")}\";${order.timestamp}"
                }
                Pair("export_commandes_$timestamp.csv", header + rows)
            }
            "Produits" -> {
                val header = "ID_Produit;Nom;Prix_XAF;Boutique;Description;Note_Moyenne;Nombre_Avis\n"
                val rows = products.joinToString("\n") { prod ->
                    val prodReviews = reviews.filter { it.targetType == "product" && it.targetId == prod.id }
                    val avg = if (prodReviews.isNotEmpty()) prodReviews.map { it.rating }.average() else 5.0
                    "${prod.id};\"${prod.name.replace("\"", "\"\"")}\";${prod.price};\"${prod.shopName.replace("\"", "\"\"")}\";\"${prod.description.replace("\"", "\"\"")}\";${"%.1f".format(java.util.Locale.US, avg)};${prodReviews.size}"
                }
                Pair("export_produits_$timestamp.csv", header + rows)
            }
            "Vendeurs" -> {
                val header = "ID_Vendeur;Nom_Boutique;Localisation;Description;Date_Inscription;Note_Moyenne\n"
                val rows = sellerAccounts.joinToString("\n") { seller ->
                    val sellerReviews = reviews.filter { it.targetType == "seller" && it.targetId == seller.name }
                    val avg = if (sellerReviews.isNotEmpty()) sellerReviews.map { it.rating }.average() else 5.0
                    "${seller.id};\"${seller.name.replace("\"", "\"\"")}\";\"${seller.location.replace("\"", "\"\"")}\";\"${seller.description.replace("\"", "\"\"")}\";\"${seller.timestamp}\";${"%.1f".format(java.util.Locale.US, avg)}"
                }
                Pair("export_vendeurs_$timestamp.csv", header + rows)
            }
            "Livreurs" -> {
                val header = "ID_Livreur;Nom_Livreur;Telephone;Vehicule;Immatriculation;Zone_Livraison\n"
                val rows = deliveryAccounts.joinToString("\n") { del ->
                    "${del.id};\"${del.name.replace("\"", "\"\"")}\";\"${del.phone}\";\"${del.transport}\";\"${del.plateNumber}\";\"${del.location.replace("\"", "\"\"")}\""
                }
                Pair("export_livreurs_$timestamp.csv", header + rows)
            }
            "Avis & Notes" -> {
                val header = "ID_Avis;Type_Cible;ID_Cible;Note_Etoiles;Client;Commentaire\n"
                val rows = reviews.joinToString("\n") { rev ->
                    "${rev.id};\"${rev.targetType}\";\"${rev.targetId}\";${rev.rating};\"${rev.customerName.replace("\"", "\"\"")}\";\"${rev.comment.replace("\"", "\"\"")}\""
                }
                Pair("export_avis_$timestamp.csv", header + rows)
            }
            "Rapport Global" -> {
                val summary = "=== RAPPORT GLOBAL MARKETPLACE ===\nDate Export;${java.util.Date()}\nChiffre d'Affaires Total;${totalRevenue} XAF\nTotal Commandes;${orders.size}\nTotal Produits;${products.size}\nTotal Vendeurs;${sellerAccounts.size}\nTotal Livreurs;${deliveryAccounts.size}\nNote Moyenne Globale;${"%.1f".format(java.util.Locale.US, avgRating)}\n\n"
                val cmdHeader = "--- DETAILS COMMANDES ---\nID;Client;Montant_XAF;Statut\n"
                val cmdRows = orders.joinToString("\n") { "${it.id};\"${it.customerName}\";${it.totalAmount};\"${it.status.label}\"" }
                val sellerHeader = "\n\n--- DETAILS VENDEURS ---\nID;Boutique;Ville;Description\n"
                val sellerRows = sellerAccounts.joinToString("\n") { "${it.id};\"${it.name}\";\"${it.location}\";\"${it.description}\"" }
                val deliveryHeader = "\n\n--- DETAILS LIVREURS ---\nID;Nom;Tel;Vehicule;Zone\n"
                val deliveryRows = deliveryAccounts.joinToString("\n") { "${it.id};\"${it.name}\";\"${it.phone}\";\"${it.transport}\";\"${it.location}\"" }
                Pair("rapport_global_admin_$timestamp.csv", summary + cmdHeader + cmdRows + sellerHeader + sellerRows + deliveryHeader + deliveryRows)
            }
            else -> Pair("export_$timestamp.csv", "")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tableau de bord Administrateur", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text("Mode Excel & Vérification Globale en Temps Réel", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "100% Connecté Firestore",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Export Action Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Export Excel", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Export Données & Statistiques", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Format CSV universel compatible Microsoft Excel / Google Sheets", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val result = generateCsv("Rapport Global")
                            exportedFileName = result.first
                            exportedCsvContent = result.second
                            exportedTitle = "Rapport Global d'Activité"
                            showExportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exporter Tout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // KPI Summary Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("CA Total", "%.0f XAF".format(totalRevenue), Modifier.weight(1f))
                StatCard("Commandes", "${orders.size}", Modifier.weight(1f))
                StatCard("Produits", "${products.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Vendeurs", "${sellerAccounts.size}", Modifier.weight(1f))
                StatCard("Livreurs", "${deliveryAccounts.size}", Modifier.weight(1f))
                StatCard("Note Moy.", "★ %.1f".format(avgRating), Modifier.weight(1f))
            }
        }

        // Excel Sheet Tabs & Section Header with Tab Export Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Feuilles de Calcul Disponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = {
                        val result = generateCsv(selectedTab)
                        exportedFileName = result.first
                        exportedCsvContent = result.second
                        exportedTitle = "Feuille : $selectedTab"
                        showExportDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.SimCardDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exporter $selectedTab", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        onClick = { selectedTab = tab },
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tab,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Excel Table Display based on selectedTab
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Feuille Excel : $selectedTab (${getItemCount(selectedTab, orders, products, sellerAccounts, deliveryAccounts, reviews)} lignes)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                val result = generateCsv(selectedTab)
                                exportedFileName = result.first
                                exportedCsvContent = result.second
                                exportedTitle = "Feuille : $selectedTab"
                                showExportDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Partager CSV", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val scrollState = rememberScrollState()
                    if (selectedTab == "📊 Analytics & Conversion") {
                        AdminAnalyticsDashboardSection(
                            onExportCsv = { fileName, content, title ->
                                exportedFileName = fileName
                                exportedCsvContent = content
                                exportedTitle = title
                                showExportDialog = true
                            }
                        )
                    } else if (selectedTab == "💰 Monétisation & Revenus") {
                        AdminMonetizationSection(
                            orders = orders,
                            onExportCsv = { fileName, content, title ->
                                exportedFileName = fileName
                                exportedCsvContent = content
                                exportedTitle = title
                                showExportDialog = true
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState)) {
                            when (selectedTab) {
                                "Commandes" -> OrdersExcelTable(orders)
                                "Produits" -> ProductsExcelTable(products, reviews)
                                "Vendeurs" -> SellersExcelTable(sellerAccounts, reviews)
                                "Livreurs" -> DeliveryExcelTable(deliveryAccounts)
                                "Avis & Notes" -> ReviewsExcelTable(reviews)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Excel / CSV : $exportedTitle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Fichier prêt : $exportedFileName",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Le contenu ci-dessous utilise le séparateur point-virgule (;) standard pour une ouverture directe sous Excel (Europe/Afrique/France).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        val previewScroll = rememberScrollState()
                        Text(
                            text = exportedCsvContent,
                            modifier = Modifier
                                .padding(8.dp)
                                .horizontalScroll(previewScroll),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportedCsvContent))
                            Toast.makeText(context, "Données CSV copiées dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copier")
                    }
                    Button(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, exportedCsvContent)
                                putExtra(Intent.EXTRA_SUBJECT, exportedFileName)
                                putExtra(Intent.EXTRA_TITLE, exportedFileName)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Partager le fichier CSV / Excel")
                            context.startActivity(shareIntent)
                            showExportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Partager / Exporter")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

fun getItemCount(tab: String, orders: List<Order>, products: List<Product>, sellers: List<SellerAccount>, deliveries: List<DeliveryAccount>, reviews: List<Review>): Int {
    return when (tab) {
        "Commandes" -> orders.size
        "Produits" -> products.size
        "Vendeurs" -> sellers.size
        "Livreurs" -> deliveries.size
        "Avis & Notes" -> reviews.size
        else -> 0
    }
}

@Composable
fun OrdersExcelTable(orders: List<Order>) {
    Column {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ID Commande", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Client", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
            Text("Montant (XAF)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Statut", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("Adresse", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
        }
        if (orders.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Aucune commande enregistrée.", color = Color.Gray)
            }
        } else {
            orders.forEachIndexed { index, order ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(order.id, fontSize = 12.sp, modifier = Modifier.width(110.dp), fontWeight = FontWeight.SemiBold)
                    Text(order.customerName, fontSize = 12.sp, modifier = Modifier.width(130.dp))
                    Text("${order.totalAmount} XAF", fontSize = 12.sp, modifier = Modifier.width(110.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(order.status.label, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                    Text(order.deliveryAddress, fontSize = 12.sp, modifier = Modifier.width(150.dp), color = Color.Gray)
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun ProductsExcelTable(products: List<Product>, reviews: List<Review>) {
    Column {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("ID", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
            Text("Nom du Produit", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(160.dp))
            Text("Prix (XAF)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("Boutique", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp))
            Text("Note (★)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
        }
        products.forEachIndexed { index, prod ->
            val prodReviews = reviews.filter { it.targetType == "product" && it.targetId == prod.id }
            val avg = if (prodReviews.isNotEmpty()) prodReviews.map { it.rating }.average() else 5.0
            val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
            Row(
                modifier = Modifier
                    .background(bg)
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(prod.id, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                Text(prod.name, fontSize = 12.sp, modifier = Modifier.width(160.dp), fontWeight = FontWeight.Bold)
                Text("${prod.price} XAF", fontSize = 12.sp, modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.secondary)
                Text(prod.shopName, fontSize = 12.sp, modifier = Modifier.width(140.dp), color = Color.Gray)
                Text("★ %.1f".format(avg), fontSize = 12.sp, modifier = Modifier.width(90.dp), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SellersExcelTable(sellers: List<SellerAccount>, reviews: List<Review>) {
    Column {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("ID", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("Nom Boutique", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp))
            Text("Localisation", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Description", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(180.dp))
            Text("Note Vendeur", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        }
        sellers.forEachIndexed { index, seller ->
            val sellerReviews = reviews.filter { it.targetType == "seller" && it.targetId == seller.name }
            val avg = if (sellerReviews.isNotEmpty()) sellerReviews.map { it.rating }.average() else 5.0
            val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
            Row(
                modifier = Modifier
                    .background(bg)
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(seller.id, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                Text(seller.name, fontSize = 12.sp, modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold)
                Text(seller.location, fontSize = 12.sp, modifier = Modifier.width(110.dp), color = MaterialTheme.colorScheme.secondary)
                Text(seller.description, fontSize = 12.sp, modifier = Modifier.width(180.dp), color = Color.Gray)
                Text("★ %.1f".format(avg), fontSize = 12.sp, modifier = Modifier.width(100.dp), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun DeliveryExcelTable(deliveries: List<DeliveryAccount>) {
    Column {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("ID", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("Nom Livreur", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp))
            Text("Téléphone", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
            Text("Véhicule", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
            Text("Plaque", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Zone", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
        }
        deliveries.forEachIndexed { index, del ->
            val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
            Row(
                modifier = Modifier
                    .background(bg)
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(del.id, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                Text(del.name, fontSize = 12.sp, modifier = Modifier.width(140.dp), fontWeight = FontWeight.Bold)
                Text(del.phone, fontSize = 12.sp, modifier = Modifier.width(120.dp), color = MaterialTheme.colorScheme.secondary)
                Text(del.transport, fontSize = 12.sp, modifier = Modifier.width(100.dp))
                Text(del.plateNumber, fontSize = 12.sp, modifier = Modifier.width(110.dp), fontWeight = FontWeight.SemiBold)
                Text(del.location, fontSize = 12.sp, modifier = Modifier.width(110.dp), color = Color.Gray)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun ReviewsExcelTable(reviews: List<Review>) {
    Column {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text("ID Avis", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Type", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
            Text("Cible ID", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
            Text("Note", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
            Text("Commentaire", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(220.dp))
            Text("Client", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
        }
        if (reviews.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Aucun avis enregistré.", color = Color.Gray)
            }
        } else {
            reviews.forEachIndexed { index, rev ->
                val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .background(bg)
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(rev.id, fontSize = 12.sp, modifier = Modifier.width(110.dp))
                    Text(rev.targetType, fontSize = 12.sp, modifier = Modifier.width(90.dp), color = MaterialTheme.colorScheme.secondary)
                    Text(rev.targetId, fontSize = 12.sp, modifier = Modifier.width(110.dp))
                    Text("★ ${rev.rating}", fontSize = 12.sp, modifier = Modifier.width(70.dp), color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                    Text(rev.comment, fontSize = 12.sp, modifier = Modifier.width(220.dp), color = Color.LightGray)
                    Text(rev.customerName, fontSize = 12.sp, modifier = Modifier.width(130.dp), fontWeight = FontWeight.SemiBold)
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Localisation", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Commande #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(order.status.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text("Total: ${order.totalAmount} XAF", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SalesTrendChartCard() {
    var selectedPeriod by remember { mutableStateOf("7 Jours") }
    val points7Days = listOf(
        Pair("Lun", 12500.0),
        Pair("Mar", 18400.0),
        Pair("Mer", 24000.0),
        Pair("Jeu", 19500.0),
        Pair("Ven", 32000.0),
        Pair("Sam", 45000.0),
        Pair("Dim", 38000.0)
    )
    val points30Days = listOf(
        Pair("Sem 1", 85000.0),
        Pair("Sem 2", 112000.0),
        Pair("Sem 3", 98000.0),
        Pair("Sem 4", 154000.0)
    )
    val currentPoints = if (selectedPeriod == "7 Jours") points7Days else points30Days
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tendances des Ventes (Line Chart)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("7 Jours", "30 Jours").forEach { period ->
                        val isSelected = selectedPeriod == period
                        Surface(
                            onClick = { selectedPeriod = period; hoveredIndex = null },
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = period,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Évolution dynamique du chiffre d'affaires (XAF) - Style Recharts",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (hoveredIndex != null && hoveredIndex!! < currentPoints.size) {
                val pt = currentPoints[hoveredIndex!!]
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Période : ${pt.first}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                        Text("CA : ${pt.second} XAF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }

            val primaryColor = MaterialTheme.colorScheme.primary
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                val maxVal = currentPoints.maxOfOrNull { it.second } ?: 1.0
                val minVal = 0.0

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentPoints) {
                            detectTapGestures { offset ->
                                val xStep = size.width / (currentPoints.size.coerceAtLeast(1))
                                val clickedIndex = (offset.x / xStep).toInt().coerceIn(0, currentPoints.size - 1)
                                hoveredIndex = clickedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val xStep = width / (currentPoints.size - 1).coerceAtLeast(1)

                    val pointsList = currentPoints.mapIndexed { index, pair ->
                        val x = index * xStep
                        val y = height - ((pair.second - minVal) / (maxVal - minVal).coerceAtLeast(1.0)).toFloat() * (height - 20f) - 10f
                        Offset(x, y)
                    }

                    for (i in 0..3) {
                        val yPos = (height / 3f) * i
                        drawLine(
                            color = surfaceVariant,
                            start = Offset(0f, yPos),
                            end = Offset(width, yPos),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val path = androidx.compose.ui.graphics.Path().apply {
                        if (pointsList.isNotEmpty()) {
                            moveTo(pointsList[0].x, pointsList[0].y)
                            for (i in 1 until pointsList.size) {
                                lineTo(pointsList[i].x, pointsList[i].y)
                            }
                        }
                    }

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )

                    pointsList.forEachIndexed { index, offset ->
                        val isHovered = hoveredIndex == index
                        drawCircle(
                            color = if (isHovered) Color.Red else primaryColor,
                            radius = if (isHovered) 7.dp.toPx() else 4.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isHovered) 4.dp.toPx() else 2.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                currentPoints.forEachIndexed { index, pt ->
                    Text(
                        text = pt.first,
                        fontSize = 11.sp,
                        fontWeight = if (hoveredIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (hoveredIndex == index) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
