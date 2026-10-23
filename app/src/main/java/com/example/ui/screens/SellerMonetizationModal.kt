package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.monetization.*
import com.example.data.payment.CentralAfricanCountry
import com.example.data.payment.PaymentOperator
import com.example.domain.model.Product
import com.example.domain.model.Shop
import com.example.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerMonetizationModal(
    shop: Shop?,
    products: List<Product>,
    viewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf("Abonnements Pro") } // "Abonnements Pro" or "Booster un Produit"
    val tabs = listOf("Abonnements Pro", "Booster un Produit", "Calculateur Rentabilité")

    // Subscription selection state
    var selectedTier by remember { mutableStateOf(SubscriptionTier.PRO) }
    var selectedOperator by remember { mutableStateOf<PaymentOperator>(PaymentOperator.ORANGE_MONEY) }
    var phoneNumber by remember { mutableStateOf("690000000") }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccessDialog by remember { mutableStateOf(false) }
    var lastTxnRef by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    // Boost selection state
    var selectedProductToBoost by remember { mutableStateOf<Product?>(products.firstOrNull()) }
    var selectedBoostPackage by remember { mutableStateOf(BoostPackage.BOOST_7_DAYS) }

    // Profitability calculator
    var estimatedMonthlySalesXaf by remember { mutableStateOf("500000") }

    AlertDialog(
        onDismissRequest = { if (!isProcessingPayment) onDismiss() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFD4AF37))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Monétisation & Croissance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(shop?.name ?: "Ma Boutique", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                IconButton(onClick = { if (!isProcessingPayment) onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                // Tab switcher
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    items(tabs) { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = tab,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(bottom = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

                when (selectedTab) {
                    "Abonnements Pro" -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Passez à la formule Pro ou VIP pour réduire la commission à 0% et doubler votre visibilité avec Orange Money & MTN MoMo.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            items(SubscriptionTier.values()) { tier ->
                                val isSelected = selectedTier == tier
                                val isFree = tier == SubscriptionTier.FREE
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTier = tier },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedTier = tier }
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(tier.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Surface(
                                                color = if (isFree) Color.Gray.copy(alpha = 0.2f) else Color(0xFFFFD700).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isFree) "0 XAF / Gratuit" else "${"%.0f".format(tier.priceXaf)} XAF / mois",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isFree) Color.DarkGray else Color(0xFFB8860B)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(tier.badgeLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        tier.perks.forEach { perk ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(perk, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedTier != SubscriptionTier.FREE) {
                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Mode de règlement Mobile Money :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PaymentOperatorCard(
                                            title = "Orange Money",
                                            subtitle = "USSD WebPay",
                                            isSelected = selectedOperator == PaymentOperator.ORANGE_MONEY,
                                            activeColor = Color(0xFFFF7900),
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedOperator = PaymentOperator.ORANGE_MONEY }
                                        )
                                        PaymentOperatorCard(
                                            title = "MTN MoMo",
                                            subtitle = "API Collection",
                                            isSelected = selectedOperator == PaymentOperator.MTN_MOMO,
                                            activeColor = Color(0xFFFFCC00),
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedOperator = PaymentOperator.MTN_MOMO }
                                        )
                                    }

                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Numéro Mobile Money Débité (+237)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    "Booster un Produit" -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                Text("1. Choisissez l'article à propulser :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (products.isEmpty()) {
                                    Text("Aucun produit publié. Publiez d'abord un article pour le booster.", color = Color.Gray, fontSize = 12.sp)
                                } else {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            products.take(6).forEach { prod ->
                                                val isProdSelected = selectedProductToBoost?.id == prod.id
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isProdSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                                        .clickable { selectedProductToBoost = prod }
                                                        .padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        RadioButton(
                                                            selected = isProdSelected,
                                                            onClick = { selectedProductToBoost = prod }
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Column {
                                                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text("${prod.price.toInt()} XAF", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                                        }
                                                    }
                                                    if (prod.isSponsored) {
                                                        Surface(color = Color(0xFFFFD700).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                            Text("⭐ Déjà Boosté", fontSize = 10.sp, color = Color(0xFFB8860B), modifier = Modifier.padding(4.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Text("2. Formule de mise en avant publicitaire :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            items(BoostPackage.values()) { pack ->
                                val isPackSelected = selectedBoostPackage == pack
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBoostPackage = pack },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(if (isPackSelected) 2.dp else 1.dp, if (isPackSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            RadioButton(selected = isPackSelected, onClick = { selectedBoostPackage = pack })
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(pack.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(pack.description, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                        Surface(color = Color(0xFF2E7D32).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                            Text("${pack.priceXaf.toInt()} XAF", modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Numéro Orange / MTN de débit (+237)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    "Calculateur Rentabilité" -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Simulateur d'Économies & Marges", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Entrez votre Chiffre d'Affaires mensuel estimé pour comparer les formules :", style = MaterialTheme.typography.bodySmall)

                                        OutlinedTextField(
                                            value = estimatedMonthlySalesXaf,
                                            onValueChange = { estimatedMonthlySalesXaf = it },
                                            label = { Text("CA Mensuel Estimé (XAF)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        val sales = estimatedMonthlySalesXaf.toDoubleOrNull() ?: 500000.0
                                        val commissionStarter = sales * 0.05
                                        val commissionPro = sales * 0.02 + 4900.0
                                        val commissionVip = 0.0 + 12500.0

                                        val savingsWithVip = (commissionStarter - commissionVip).coerceAtLeast(0.0)

                                        Divider()

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("• Starter (5% commission) : ${commissionStarter.toInt()} XAF de frais", fontSize = 12.sp)
                                            Text("• Pro (2% + 4900 XAF) : ${commissionPro.toInt()} XAF de frais", fontSize = 12.sp)
                                            Text("• VIP Diamant (0% + 12500 XAF) : ${commissionVip.toInt()} XAF de frais fixes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }

                                        if (savingsWithVip > 0) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f))
                                            ) {
                                                Text(
                                                    "💰 Vous économisez ${savingsWithVip.toInt()} XAF / mois en optant pour l'abonnement VIP Diamant !",
                                                    modifier = Modifier.padding(10.dp),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1B5E20)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == "Abonnements Pro") {
                Button(
                    onClick = {
                        if (selectedTier == SubscriptionTier.FREE) {
                            viewModel.upgradeSellerSubscription(shop?.id ?: "s_default", SubscriptionTier.FREE)
                            Toast.makeText(context, "Formule Starter Gratuit sélectionnée !", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            isProcessingPayment = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(2000)
                                val txnId = "SUB-TXN-${System.currentTimeMillis() % 1000000}"
                                MonetizationManager.subscribeSeller(
                                    sellerId = shop?.id ?: "s_default",
                                    sellerName = shop?.name ?: "Boutique",
                                    tier = selectedTier,
                                    operator = selectedOperator,
                                    txnId = txnId
                                )
                                viewModel.upgradeSellerSubscription(shop?.id ?: "s_default", selectedTier)
                                lastTxnRef = txnId
                                successMessage = "Abonnement ${selectedTier.title} activé avec succès !"
                                isProcessingPayment = false
                                paymentSuccessDialog = true
                            }
                        }
                    },
                    enabled = !isProcessingPayment && (selectedTier == SubscriptionTier.FREE || phoneNumber.length >= 8),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Débit USSD en cours...")
                    } else {
                        Text(if (selectedTier == SubscriptionTier.FREE) "Choisir Starter" else "Payer ${selectedTier.priceXaf.toInt()} XAF")
                    }
                }
            } else if (selectedTab == "Booster un Produit") {
                Button(
                    onClick = {
                        if (selectedProductToBoost != null) {
                            isProcessingPayment = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(2000)
                                val txnId = "BOOST-${System.currentTimeMillis() % 1000000}"
                                MonetizationManager.boostProduct(
                                    productId = selectedProductToBoost!!.id,
                                    productName = selectedProductToBoost!!.name,
                                    shopName = shop?.name ?: "Boutique",
                                    boostPackage = selectedBoostPackage,
                                    operator = selectedOperator,
                                    txnId = txnId
                                )
                                viewModel.applyProductBoost(selectedProductToBoost!!.id, selectedBoostPackage.title)
                                lastTxnRef = txnId
                                successMessage = "${selectedBoostPackage.title} activé pour '${selectedProductToBoost!!.name}' !"
                                isProcessingPayment = false
                                paymentSuccessDialog = true
                            }
                        }
                    },
                    enabled = !isProcessingPayment && selectedProductToBoost != null && phoneNumber.length >= 8,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.Black)
                ) {
                    if (isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validation...")
                    } else {
                        Text("Activer Boost (${selectedBoostPackage.priceXaf.toInt()} XAF)")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isProcessingPayment) onDismiss() }, enabled = !isProcessingPayment) {
                Text("Fermer")
            }
        }
    )

    // Success Confirmation Dialog
    if (paymentSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                paymentSuccessDialog = false
                onDismiss()
            },
            title = { Text("Paiement & Activation Réussis ! 🎉", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(successMessage)
                    Text("🧾 Référence Mobile Money : $lastTxnRef", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("✅ Vos privilèges sont désormais actifs et synchronisés avec Firestore en temps réel.")
                }
            },
            confirmButton = {
                Button(onClick = {
                    paymentSuccessDialog = false
                    onDismiss()
                }) {
                    Text("Génial, continuer")
                }
            }
        )
    }
}

@Composable
private fun PaymentOperatorCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) activeColor else Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = if (isSelected) activeColor else Color.Gray, modifier = Modifier.size(20.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
