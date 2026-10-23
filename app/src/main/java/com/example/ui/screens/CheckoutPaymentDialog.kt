package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.BankRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.BankAccount
import com.example.domain.model.CartItem
import com.example.domain.model.Order
import kotlinx.coroutines.launch

enum class PaymentChoice {
    CASH_ON_DELIVERY,
    BANK_TRANSFER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentDialog(
    cart: List<CartItem>,
    onDismiss: () -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bankRepo = remember { BankRepository(context) }
    val orderRepo = remember { OrderRepository(context) }

    val totalAmount = remember(cart) { cart.sumOf { it.product.price * it.quantity } }

    var selectedChoice by remember { mutableStateOf(PaymentChoice.CASH_ON_DELIVERY) }
    var customerName by remember { mutableStateOf("Client NboaShop") }
    var deliveryAddress by remember { mutableStateOf("Akwa, Douala") }
    var deliveryCity by remember { mutableStateOf("Douala") }
    var contactPhone by remember { mutableStateOf("+237 690 00 00 00") }

    // Bank transfer fields
    var bankAccounts by remember { mutableStateOf<List<BankAccount>>(bankRepo.defaultCemacBanks) }
    var selectedBank by remember { mutableStateOf(bankAccounts.first()) }
    var bankReference by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var confirmedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        bankRepo.observeBankAccounts().collect {
            if (it.isNotEmpty()) {
                bankAccounts = it
                if (selectedBank !in it) {
                    selectedBank = it.first()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            if (confirmedOrder != null) {
                // Success Modal Screen showing OTP code & Order tracking receipt
                val order = confirmedOrder!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Commande Confirmée !",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Réf. #${order.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (order.paymentMethod == "CASH_ON_DELIVERY") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "VOTRE CODE OTP LIVREUR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = order.deliveryOtp,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Communiquez ce code à 4 chiffres au livreur à la réception de votre colis pour valider le paiement de ${"%,.0f".format(order.totalAmount)} XAF.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "DÉTAILS DU VIREMENT CEMAC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Banque : ${order.bankAccountName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Référence enregistrée : ${order.bankTransferRef.ifBlank { "En attente" }}", fontSize = 13.sp)
                                Text("Montant à virer : ${"%,.0f".format(order.totalAmount)} XAF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Adresse de livraison : ${order.deliveryAddress}, ${order.deliveryCity}", style = MaterialTheme.typography.bodySmall)
                            Text("Téléphone de contact : ${order.contactPhone}", style = MaterialTheme.typography.bodySmall)
                            Text("Statut Firestore : ${order.status.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onOrderPlaced(order)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Accéder au Suivi de Commande")
                    }
                }
            } else {
                // Checkout Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Validation de Commande",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }

                    Text(
                        text = "Total à régler : ${"%,.0f".format(totalAmount)} XAF (${cart.size} article(s))",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Mode de règlement :", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedChoice = PaymentChoice.CASH_ON_DELIVERY },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedChoice == PaymentChoice.CASH_ON_DELIVERY)
                                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (selectedChoice == PaymentChoice.CASH_ON_DELIVERY)
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("À la livraison", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Code OTP sécurisé", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedChoice = PaymentChoice.BANK_TRANSFER },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedChoice == PaymentChoice.BANK_TRANSFER)
                                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (selectedChoice == PaymentChoice.BANK_TRANSFER)
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Virement bancaire", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Banques CEMAC", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Coordinates
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nom & Prénom du destinataire") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deliveryCity,
                            onValueChange = { deliveryCity = it },
                            label = { Text("Ville") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Téléphone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Quartier & Adresse de livraison") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedChoice == PaymentChoice.BANK_TRANSFER) {
                        Text("Sélectionnez la banque de virement :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        bankAccounts.forEach { bank ->
                            val isSelected = selectedBank.id == bank.id
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedBank = bank },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(bank.bankName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("IBAN", bank.iban))
                                                Toast.makeText(context, "IBAN copié !", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text("Bénéficiaire : ${bank.accountName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("IBAN : ${bank.iban}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                                    if (bank.rib.isNotBlank()) Text("RIB : ${bank.rib}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bankReference,
                            onValueChange = { bankReference = it },
                            label = { Text("Numéro / Réf de transaction bancaire") },
                            placeholder = { Text("Ex: VIR-849201-AFR") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Effectuez le virement du montant exact vers l'IBAN indiqué, puis entrez le numéro de reçu pour confirmation par le marchand.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Un code secret OTP à 4 chiffres vous sera délivré immédiatement pour valider la livraison avec le livreur.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isSubmitting = true
                            val itemsSummary = cart.joinToString(", ") { "${it.quantity}x ${it.product.displayTitle}" }
                            coroutineScope.launch {
                                val result = orderRepo.createOrder(
                                    customerName = customerName,
                                    sellerId = "seller_official",
                                    totalAmount = totalAmount,
                                    paymentMethod = selectedChoice.name,
                                    deliveryAddress = deliveryAddress,
                                    deliveryCity = deliveryCity,
                                    contactPhone = contactPhone,
                                    itemsSummary = itemsSummary,
                                    bankAccountName = if (selectedChoice == PaymentChoice.BANK_TRANSFER) selectedBank.bankName else "",
                                    bankTransferRef = if (selectedChoice == PaymentChoice.BANK_TRANSFER) bankReference else ""
                                )
                                isSubmitting = false
                                if (result.isSuccess) {
                                    confirmedOrder = result.getOrNull()
                                } else {
                                    Toast.makeText(context, "Erreur création commande: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting && deliveryAddress.isNotBlank() && contactPhone.isNotBlank(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (selectedChoice == PaymentChoice.CASH_ON_DELIVERY)
                                    "Confirmer la Commande & Obtenir le Code OTP"
                                else
                                    "Enregistrer le Virement Bancaire",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
