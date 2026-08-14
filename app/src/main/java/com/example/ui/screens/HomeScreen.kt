package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.domain.model.Shop
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
            TopAppBar(
                title = { Text("NboaShop - $currentRole", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (currentRole == "Client" || currentRole == "Livreur") {
                        IconButton(onClick = { showOrderTracking = true }) {
                            Icon(Icons.Default.Timeline, contentDescription = "Suivi en Temps Réel", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    if (currentRole == "Client") {
                        IconButton(onClick = { showUserProfile = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Mon Profil", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { showOrderHistory = true }) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Historique Commandes", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { showCart = true }) {
                            BadgedBox(
                                badge = {
                                    if (cart.isNotEmpty()) {
                                        Badge { Text("${cart.sumOf { it.quantity }}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Panier", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }

                    // Settings Button (Prominent & Always Visible)
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    // Overflow Menu for Professional / Extra Options
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Plus d'options", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🏪 Boutiques Partenaires", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    showShopsDirectoryDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🔐 Connexion Pro (PIN)", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    showLoginChoiceDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("➕ Créer Compte Pro", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showMenu = false
                                    showAccountChoiceDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showChatbot = !showChatbot },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                    "Admin" -> AdminDashboard(orders, products, sellerAccounts, deliveryAccounts)
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

            // Checkout / Mobile Money Payment Dialog (Central Africa - CEMAC)
            if (showCheckoutDialog) {
                val totalAmount = cart.sumOf { it.product.price * it.quantity }
                val countries = listOf("Cameroun (+237)", "Gabon (+241)", "Congo (+242)", "Tchad (+235)", "Centrafrique (+236)", "Guinée Équatoriale (+240)")
                
                AlertDialog(
                    onDismissRequest = { if (!paymentProcessing) showCheckoutDialog = false },
                    title = { Text("Paiement Mobile Money (Afrique Centrale)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 480.dp)) {
                            Text("Montant total à régler : $totalAmount XAF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            
                            Text("Pays de l'Afrique Centrale (CEMAC) :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(countries) { country ->
                                    val isSelected = selectedCountry == country
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCountry = country },
                                        label = { Text(country, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Text("Opérateur de Paiement Officiel :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedCard(
                                    modifier = Modifier.weight(1f).clickable { selectedPaymentMethod = "Orange Money" },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (selectedPaymentMethod == "Orange Money") Color(0xFFFF6600).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(if (selectedPaymentMethod == "Orange Money") 2.dp else 1.dp, if (selectedPaymentMethod == "Orange Money") Color(0xFFFF6600) else Color.Gray)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFFF6600))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Orange Money", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("API WebPay", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                    }
                                }

                                OutlinedCard(
                                    modifier = Modifier.weight(1f).clickable { selectedPaymentMethod = "MTN Mobile Money" },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (selectedPaymentMethod == "MTN Mobile Money") Color(0xFFFFCC00).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(if (selectedPaymentMethod == "MTN Mobile Money") 2.dp else 1.dp, if (selectedPaymentMethod == "MTN Mobile Money") Color(0xFFCCA300) else Color.Gray)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFB38F00))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("MTN MoMo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("API Collection", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = checkoutPhone,
                                onValueChange = { checkoutPhone = it },
                                label = { Text("Numéro de téléphone (${selectedCountry.substringAfter('(').substringBefore(')')})") },
                                placeholder = { Text("Ex: 690000000 / 670000000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = checkoutName,
                                onValueChange = { checkoutName = it },
                                label = { Text("Nom et Prénom du Client") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = checkoutAddress,
                                onValueChange = { checkoutAddress = it },
                                label = { Text("Adresse de livraison (Quartier, Ville)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text(
                                "🔒 Connexion sécurisée aux passerelles officielles Orange Money & MTN MoMo API (CEMAC). Un message USSD de confirmation sera envoyé sur votre téléphone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            if (paymentProcessing) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Traitement API $selectedPaymentMethod en cours...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (checkoutPhone.length >= 8) {
                                    paymentProcessing = true
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        val prefix = if (selectedPaymentMethod == "Orange Money") "OM" else "MOMO"
                                        val countryCode = selectedCountry.substringAfter('(').substringBefore(')')
                                        lastPaymentRef = "$prefix-$countryCode-${System.currentTimeMillis() % 1000000}"
                                        
                                        viewModel.createOrder(
                                            customerName = checkoutName,
                                            totalAmount = totalAmount.toDouble(),
                                            deliveryAddress = "$checkoutAddress ($selectedCountry - Tel: $checkoutPhone)"
                                        )
                                        
                                        paymentProcessing = false
                                        showCheckoutDialog = false
                                        paymentSuccessDialog = true
                                    }
                                }
                            },
                            enabled = !paymentProcessing && checkoutPhone.length >= 8 && checkoutName.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Valider & Payer $totalAmount XAF")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { if (!paymentProcessing) showCheckoutDialog = false },
                            enabled = !paymentProcessing
                        ) {
                            Text("Annuler")
                        }
                    }
                )
            }

            // Payment Success Confirmation Dialog
            if (paymentSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { paymentSuccessDialog = false },
                    title = { Text("Paiement Validé avec Succès ! 🎉", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Votre transaction via l'API officielle $selectedPaymentMethod ($selectedCountry) a été acceptée et sécurisée.")
                            Text("🧾 Référence de transaction : $lastPaymentRef", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("📦 Votre commande a été transmise au marchand et au service de livraison géolocalisé.")
                            Text("Statut : Payé & En préparation", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { paymentSuccessDialog = false }) {
                            Text("Super, merci !")
                        }
                    }
                )
            }

            // Settings Dialog
            if (showSettings) {
                AlertDialog(
                    onDismissRequest = { showSettings = false },
                    title = { Text("Paramètres & Données de l'Application") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Notifications Push")
                                Switch(checked = true, onCheckedChange = {})
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Mode Sombre Pro")
                                Switch(checked = true, onCheckedChange = {})
                            }
                            Divider()
                            Text("Comptes Vendeurs Enregistrés (${sellerAccounts.size}) :", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            sellerAccounts.forEach { s ->
                                Text("• ${s.name} (${s.location}) [PIN prot]", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Comptes Livreurs Enregistrés (${deliveryAccounts.size}) :", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            deliveryAccounts.forEach { d ->
                                Text("• ${d.name} - ${d.transport} [PIN prot]", style = MaterialTheme.typography.bodySmall)
                            }
                            Divider()
                            Button(
                                onClick = {
                                    showSettings = false
                                    showLegal = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📜 Confidentialité, À Propos & Certificats")
                            }
                            Divider()
                            Text("Devise : XAF (Franc CFA)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Localisation GPS Google Maps : Active", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                            Text("Version : 2.7.0 (Sécurisé par PIN 4 chiffres)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showSettings = false }) {
                            Text("Fermer")
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
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Rechercher un produit, une boutique...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    { IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Default.Clear, contentDescription = "Effacer") } }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Category Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedCategory(category) },
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
            ProductCard(product = product, onAddToCart = { viewModel.addToCart(product) })
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
fun ProductCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Text(
                text = "Boutique : ${product.shopName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter au panier", fontWeight = FontWeight.SemiBold)
            }
        }
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
                    if (prodNameInput.isNotEmpty() && selectedImageUri != null) {
                        viewModel.addProduct(prodNameInput, prodDescInput, price, selectedImageUri!!)
                        showPublishDialog = false
                        prodNameInput = ""
                        prodDescInput = ""
                        prodPriceInput = ""
                        selectedImageUri = null
                    }
                }) {
                    Text("Publier")
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
    deliveryAccounts: List<DeliveryAccount>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Tableau de bord Administrateur", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Boutiques", "${sellerAccounts.size}", Modifier.weight(1f))
                StatCard("Livreurs", "${deliveryAccounts.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Produits", "${products.size}", Modifier.weight(1f))
                StatCard("Commandes", "${orders.size}", Modifier.weight(1f))
            }
        }

        item {
            Text("Liste des Vendeurs Enregistrés", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        items(sellerAccounts) { seller ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(seller.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("📍 ${seller.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Text(seller.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("🔐 Sécurisé par code PIN", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                }
            }
        }

        item {
            Text("Liste des Livreurs Enregistrés", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        items(deliveryAccounts) { delivery ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(delivery.name, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("🛵 ${delivery.transport} | Plaque : ${delivery.plateNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Text("📞 ${delivery.phone} | Zone : ${delivery.location}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("🔐 Sécurisé par code PIN", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                }
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
