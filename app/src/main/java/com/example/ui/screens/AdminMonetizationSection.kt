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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.monetization.*
import com.example.data.payment.CentralAfricanCountry
import com.example.data.payment.PaymentOperator
import com.example.domain.model.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminMonetizationSection(
    orders: List<Order>,
    onExportCsv: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Reactive snapshot of revenues
    var totalRevenue by remember { mutableStateOf(MonetizationManager.getTotalPlatformRevenue()) }
    var availableBalance by remember { mutableStateOf(MonetizationManager.getAvailableBalance()) }
    var totalWithdrawn by remember { mutableStateOf(MonetizationManager.getTotalWithdrawn()) }

    // Commission / Fee config states
    var commissionRate by remember { mutableStateOf(MonetizationManager.config.standardCommissionRate.toFloat()) }
    var serviceFeeXaf by remember { mutableStateOf(MonetizationManager.config.platformBuyerServiceFee.toString()) }

    // Admin Cashout / Payout dialog
    var showPayoutDialog by remember { mutableStateOf(false) }
    var payoutAmount by remember { mutableStateOf("") }
    var payoutPhone by remember { mutableStateOf("690000000") }
    var selectedPayoutOperator by remember { mutableStateOf<PaymentOperator>(PaymentOperator.ORANGE_MONEY) }
    var selectedPayoutCountry by remember { mutableStateOf<CentralAfricanCountry>(CentralAfricanCountry.CAMEROON) }
    var isProcessingPayout by remember { mutableStateOf(false) }

    fun refreshStats() {
        totalRevenue = MonetizationManager.getTotalPlatformRevenue()
        availableBalance = MonetizationManager.getAvailableBalance()
        totalWithdrawn = MonetizationManager.getTotalWithdrawn()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Financial Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFB8860B))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Revenus & Monétisation Plateforme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Modèle 4 Piliers : Commissions, Abonnements, Boosts & Frais", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Button(
                        onClick = {
                            payoutAmount = availableBalance.toInt().toString()
                            showPayoutDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retirer Gains", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f))

                // KPI Triplets
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RevenueKpiCard(
                        title = "Gains Bruts Plateforme",
                        amount = "${"%.0f".format(totalRevenue)} XAF",
                        accentColor = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f)
                    )
                    RevenueKpiCard(
                        title = "Solde Retirable Net",
                        amount = "${"%.0f".format(availableBalance)} XAF",
                        accentColor = Color(0xFFD4AF37),
                        modifier = Modifier.weight(1f)
                    )
                    RevenueKpiCard(
                        title = "Total Déjà Viré",
                        amount = "${"%.0f".format(totalWithdrawn)} XAF",
                        accentColor = Color(0xFF0D47A1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4 Monetization Pillars Breakdown Cards
        Text("Répartition par Source de Revenus", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val commissions = MonetizationManager.revenueEntries.filter { it.source == "COMMISSION" }.sumOf { it.amount }
            val subscriptions = MonetizationManager.revenueEntries.filter { it.source == "SUBSCRIPTION" }.sumOf { it.amount }
            val boosts = MonetizationManager.revenueEntries.filter { it.source == "BOOST" }.sumOf { it.amount }
            val fees = MonetizationManager.revenueEntries.filter { it.source == "SERVICE_FEE" }.sumOf { it.amount }

            PillarSummaryCard("Commissions (5%)", "${commissions.toInt()} XAF", Icons.Default.Percent, Color(0xFF2E7D32), Modifier.weight(1f))
            PillarSummaryCard("Abonnements Pro", "${subscriptions.toInt()} XAF", Icons.Default.WorkspacePremium, Color(0xFFB8860B), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val boosts = MonetizationManager.revenueEntries.filter { it.source == "BOOST" }.sumOf { it.amount }
            val fees = MonetizationManager.revenueEntries.filter { it.source == "SERVICE_FEE" }.sumOf { it.amount }

            PillarSummaryCard("Boosts Produits", "${boosts.toInt()} XAF", Icons.Default.RocketLaunch, Color(0xFFE65100), Modifier.weight(1f))
            PillarSummaryCard("Frais de Service", "${fees.toInt()} XAF", Icons.Default.Security, Color(0xFF1565C0), Modifier.weight(1f))
        }

        // Platform Pricing & Commission Control Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ Paramètres Monétaires Plateforme (En Direct)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Button(
                        onClick = {
                            val fee = serviceFeeXaf.toDoubleOrNull() ?: 200.0
                            MonetizationManager.config = PlatformConfig(
                                standardCommissionRate = commissionRate.toDouble(),
                                platformBuyerServiceFee = fee
                            )
                            Toast.makeText(context, "Tarification mise à jour en temps réel !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Appliquer", fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Taux de commission : ${commissionRate.toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Slider(
                            value = commissionRate,
                            onValueChange = { commissionRate = it },
                            valueRange = 0f..15f,
                            steps = 14
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frais de service acheteur :", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = serviceFeeXaf,
                            onValueChange = { serviceFeeXaf = it },
                            label = { Text("Frais fixes (XAF)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Live Revenue Ledger Header with Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Journal des Flux Financiers (Live)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            OutlinedButton(
                onClick = {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val header = "ID_Flux;Source;Montant_XAF;Description;Reference;Operateur_Paiement;Date\n"
                    val rows = MonetizationManager.revenueEntries.joinToString("\n") { entry ->
                        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                        "${entry.id};\"${entry.source}\";${entry.amount};\"${entry.description.replace("\"", "\"\"")}\";\"${entry.referenceId}\";\"${entry.paymentOperator}\";\"$dateStr\""
                    }
                    onExportCsv("export_monetisation_$timestamp.csv", header + rows, "Journal de Monétisation & Revenus")
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export Excel CSV", fontSize = 11.sp)
            }
        }

        // Ledger Table Entries
        if (MonetizationManager.revenueEntries.isEmpty()) {
            Text("Aucune transaction enregistrée pour l'instant.", color = Color.Gray, fontSize = 12.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MonetizationManager.revenueEntries.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val (icon, iconColor) = when (entry.source) {
                                    "SUBSCRIPTION" -> Pair(Icons.Default.WorkspacePremium, Color(0xFFB8860B))
                                    "BOOST" -> Pair(Icons.Default.RocketLaunch, Color(0xFFE65100))
                                    "COMMISSION" -> Pair(Icons.Default.Percent, Color(0xFF2E7D32))
                                    else -> Pair(Icons.Default.Security, Color(0xFF1565C0))
                                }
                                Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(entry.description, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                                    Text("$dateStr • Via ${entry.paymentOperator} (${entry.referenceId})", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "+${entry.amount.toInt()} XAF",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

    // Admin Payout / Cashout Dialog
    if (showPayoutDialog) {
        AlertDialog(
            onDismissRequest = { if (!isProcessingPayout) showPayoutDialog = false },
            title = { Text("Retrait des Gains Administrateur", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Transférez les revenus collectés par l'application vers votre numéro Orange Money ou MTN MoMo personnel :", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = payoutAmount,
                        onValueChange = { payoutAmount = it },
                        label = { Text("Montant à retirer (XAF)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Opérateur de virement instantané :", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPayoutOperator = PaymentOperator.ORANGE_MONEY },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedPayoutOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(if (selectedPayoutOperator == PaymentOperator.ORANGE_MONEY) 2.dp else 1.dp, if (selectedPayoutOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFFF7900))
                                Text("Orange Money", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPayoutOperator = PaymentOperator.MTN_MOMO },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedPayoutOperator == PaymentOperator.MTN_MOMO) Color(0xFFFFCC00).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(if (selectedPayoutOperator == PaymentOperator.MTN_MOMO) 2.dp else 1.dp, if (selectedPayoutOperator == PaymentOperator.MTN_MOMO) Color(0xFFC69200) else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFC69200))
                                Text("MTN MoMo", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = payoutPhone,
                        onValueChange = { payoutPhone = it },
                        label = { Text("Numéro Mobile Money Récepteur (+237)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isProcessingPayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Virement en cours vers votre compte...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payoutAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && amount <= availableBalance && payoutPhone.length >= 8) {
                            isProcessingPayout = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(2000)
                                MonetizationManager.requestAdminPayout(
                                    amount = amount,
                                    phone = payoutPhone,
                                    operator = selectedPayoutOperator,
                                    country = selectedPayoutCountry
                                )
                                refreshStats()
                                isProcessingPayout = false
                                showPayoutDialog = false
                                Toast.makeText(context, "Virement de ${amount.toInt()} XAF effectué avec succès !", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Montant invalide ou solde insuffisant", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isProcessingPayout && payoutPhone.length >= 8 && (payoutAmount.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Confirmer Virement")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isProcessingPayout) showPayoutDialog = false }, enabled = !isProcessingPayout) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun RevenueKpiCard(
    title: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = accentColor, maxLines = 1)
        }
    }
}

@Composable
private fun PillarSummaryCard(
    title: String,
    amount: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = Color.Gray)
                Text(amount, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            }
        }
    }
}
