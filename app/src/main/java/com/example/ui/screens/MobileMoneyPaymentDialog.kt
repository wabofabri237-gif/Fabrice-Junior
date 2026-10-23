package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.payment.*
import com.example.domain.model.CartItem
import com.example.domain.model.Order
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileMoneyPaymentDialog(
    cart: List<CartItem>,
    onDismiss: () -> Unit,
    onPaymentSuccess: (Order, PaymentTransaction) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalAmount = remember(cart) { cart.sumOf { it.product.price * it.quantity } }

    var selectedCountry by remember { mutableStateOf(CentralAfricanCountry.CAMEROON) }
    var selectedOperator by remember { mutableStateOf(PaymentOperator.ORANGE_MONEY) }
    var phoneNumber by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }

    // Payment process state
    var isProcessing by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) } // 1: Input, 2: USSD Push Waiting, 3: Completed Receipt
    var statusMessage by remember { mutableStateOf("") }
    var transactionStatus by remember { mutableStateOf(PaymentTransactionStatus.INITIATED) }
    var ussdCountdown by remember { mutableStateOf(30) }
    var completedTransaction by remember { mutableStateOf<PaymentTransaction?>(null) }
    var completedOrder by remember { mutableStateOf<Order?>(null) }

    // Auto-detect operator based on phone prefix
    LaunchedEffect(phoneNumber, selectedCountry) {
        val detected = MobileMoneyPaymentManager.detectSuggestedOperator(phoneNumber, selectedCountry)
        if (detected != null && detected != selectedOperator) {
            selectedOperator = detected
        }
    }

    // Handle USSD countdown timer when waiting for PIN approval
    LaunchedEffect(currentStep, isProcessing) {
        if (currentStep == 2 && isProcessing) {
            ussdCountdown = 25
            while (ussdCountdown > 0 && isProcessing) {
                delay(1000)
                ussdCountdown--
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        containerColor = AfriDarkGray,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (currentStep == 3) "Paiement Validé !" else "Paiement Mobile Money",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AfriWhite
                    )
                    Text(
                        text = "Zone CEMAC • Franc CFA (XAF)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AfriGold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Amount Summary Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AfriBlack),
                    border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Montant Net à Payer", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "${"%,.0f".format(totalAmount)} XAF",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = AfriGold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AfriRed.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, AfriRed)
                        ) {
                            Text(
                                "0% Frais",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AfriWhite
                            )
                        }
                    }
                }

                when (currentStep) {
                    // STEP 1: Country, Operator, and Payer Input
                    1 -> {
                        // 1. Country Selection (CEMAC)
                        Text(
                            "1. Sélectionnez votre pays (Afrique Centrale) :",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = AfriWhite
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(CentralAfricanCountry.values()) { country ->
                                val isSelected = selectedCountry == country
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCountry = country },
                                    label = { Text("${country.flagEmoji} ${country.countryName}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AfriRed,
                                        selectedLabelColor = AfriWhite,
                                        containerColor = AfriBlack,
                                        labelColor = Color.LightGray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) AfriGold else Color.DarkGray
                                    )
                                )
                            }
                        }

                        // 2. Operator Selection (Orange Money vs MTN MoMo)
                        Text(
                            "2. Choisissez votre opérateur :",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = AfriWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Orange Money Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOperator = PaymentOperator.ORANGE_MONEY },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedOperator == PaymentOperator.ORANGE_MONEY)
                                        Color(0xFFFF7900).copy(alpha = 0.2f) else AfriBlack
                                ),
                                border = BorderStroke(
                                    width = if (selectedOperator == PaymentOperator.ORANGE_MONEY) 2.dp else 1.dp,
                                    color = if (selectedOperator == PaymentOperator.ORANGE_MONEY)
                                        Color(0xFFFF7900) else Color.DarkGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFF7900),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("OM", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Orange Money", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AfriWhite)
                                    Text("USSD / WebPay", fontSize = 9.sp, color = Color(0xFFFF7900))
                                }
                            }

                            // MTN MoMo Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOperator = PaymentOperator.MTN_MOMO },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedOperator == PaymentOperator.MTN_MOMO)
                                        Color(0xFFFFCC00).copy(alpha = 0.2f) else AfriBlack
                                ),
                                border = BorderStroke(
                                    width = if (selectedOperator == PaymentOperator.MTN_MOMO) 2.dp else 1.dp,
                                    color = if (selectedOperator == PaymentOperator.MTN_MOMO)
                                        Color(0xFFFFCC00) else Color.DarkGray
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFCC00),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("MoMo", fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color.Black)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("MTN MoMo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AfriWhite)
                                    Text("OpenAPI Push", fontSize = 9.sp, color = Color(0xFFFFCC00))
                                }
                            }
                        }

                        // 3. Inputs (Phone, Name, Address)
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Numéro ${selectedOperator.displayName} (${selectedCountry.dialCode})") },
                            placeholder = { Text("Ex: ${selectedCountry.exampleNumber}") },
                            leadingIcon = {
                                Text(
                                    text = "${selectedCountry.flagEmoji} ${selectedCountry.dialCode}",
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                                    fontSize = 12.sp,
                                    color = AfriGold,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AfriGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = AfriGold,
                                focusedTextColor = AfriWhite,
                                unfocusedTextColor = AfriWhite
                            )
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Nom complet du client") },
                            placeholder = { Text("Ex: Paul Biya / Alice Manga") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AfriGold)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AfriGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = AfriGold,
                                focusedTextColor = AfriWhite,
                                unfocusedTextColor = AfriWhite
                            )
                        )

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Adresse de livraison (Quartier, Ville)") },
                            placeholder = { Text("Ex: Bonapriso, Douala / Bastos, Yaoundé") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AfriGold)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AfriGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = AfriGold,
                                focusedTextColor = AfriWhite,
                                unfocusedTextColor = AfriWhite
                            )
                        )

                        // USSD Code Helper Note
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1B1B1B),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AfriGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Code USSD de validation : ${selectedCountry.getUssdForOperator(selectedOperator)} • Sécurisé par token cryptographique.",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    // STEP 2: USSD Live Prompt & Firestore Handshake
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Animated Pulse USSD Circle
                            Surface(
                                shape = CircleShape,
                                color = if (selectedOperator == PaymentOperator.ORANGE_MONEY)
                                    Color(0xFFFF7900).copy(alpha = 0.2f) else Color(0xFFFFCC00).copy(alpha = 0.2f),
                                border = BorderStroke(
                                    2.dp,
                                    if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00)
                                ),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(56.dp),
                                        color = if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00),
                                        strokeWidth = 3.dp
                                    )
                                    Icon(
                                        Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Text(
                                "Push USSD envoyé au ${selectedCountry.dialCode} $phoneNumber",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = AfriWhite
                            )

                            // Interactive USSD Simulator Box
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AfriBlack),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            selectedOperator.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00)
                                        )
                                        Text(
                                            "Expire dans: ${ussdCountdown}s",
                                            fontSize = 11.sp,
                                            color = AfriRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Message USSD : « Confirmez-vous le paiement de ${"%,.0f".format(totalAmount)} XAF à NboaShop Marketplace ? Entrez votre code secret. »",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF222222)
                                    ) {
                                        Text(
                                            "Si aucun pop-up n'apparaît, composez directement : ${selectedCountry.getUssdForOperator(selectedOperator)}",
                                            modifier = Modifier.padding(6.dp),
                                            fontSize = 10.sp,
                                            color = AfriGold
                                        )
                                    }
                                }
                            }

                            Text(
                                statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }

                    // STEP 3: Payment Success Receipt
                    3 -> {
                        val txn = completedTransaction
                        val ord = completedOrder

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1B5E20).copy(alpha = 0.3f),
                                border = BorderStroke(2.dp, Color(0xFF4CAF50)),
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Text(
                                "Transaction Réussie !",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color(0xFF81C784),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Digital Receipt Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AfriBlack),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Référence TXN :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            txn?.transactionId ?: "N/A",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AfriGold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Numéro Commande :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            ord?.id ?: "N/A",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AfriWhite
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Opérateur :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${txn?.operator?.displayName} (${txn?.country?.flagEmoji})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (txn?.operator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Montant Débité :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${"%,.0f".format(totalAmount)} XAF",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AfriGold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Statut Firestore :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "Payé & Synchronisé (Live)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }

                            // Copy receipt reference button
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("TransactionRef", txn?.transactionId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Référence copiée dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AfriGold.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AfriGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copier la référence du reçu", color = AfriGold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (currentStep) {
                1 -> {
                    Button(
                        onClick = {
                            if (phoneNumber.length >= 8 && customerName.isNotBlank()) {
                                currentStep = 2
                                isProcessing = true
                                val generatedOrderId = "CMD-${System.currentTimeMillis() % 100000}"

                                coroutineScope.launch {
                                    val result = MobileMoneyPaymentManager.processPayment(
                                        orderId = generatedOrderId,
                                        amount = totalAmount,
                                        operator = selectedOperator,
                                        country = selectedCountry,
                                        phoneNumber = phoneNumber,
                                        customerName = customerName,
                                        deliveryAddress = "$deliveryAddress (${selectedCountry.flagEmoji} ${selectedCountry.countryName} - Tel: ${selectedCountry.dialCode}$phoneNumber)",
                                        onStatusUpdate = { status, msg ->
                                            transactionStatus = status
                                            statusMessage = msg
                                        }
                                    )

                                    if (result.isSuccess) {
                                        val txn = result.getOrNull()!!
                                        val createdOrder = Order(
                                            id = generatedOrderId,
                                            customerName = customerName,
                                            status = com.example.domain.model.OrderStatus.PAID,
                                            totalAmount = totalAmount,
                                            deliveryAddress = "$deliveryAddress (${selectedCountry.flagEmoji} ${selectedCountry.countryName} - Tel: ${selectedCountry.dialCode}$phoneNumber)",
                                            paymentProvider = selectedOperator.displayName,
                                            transactionRef = txn.transactionId,
                                            payerPhone = "${selectedCountry.dialCode}$phoneNumber",
                                            country = selectedCountry.countryName
                                        )

                                        completedTransaction = txn
                                        completedOrder = createdOrder
                                        isProcessing = false
                                        currentStep = 3
                                        onPaymentSuccess(createdOrder, txn)
                                    } else {
                                        isProcessing = false
                                        Toast.makeText(context, "Échec du paiement: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        currentStep = 1
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Veuillez saisir votre numéro et votre nom", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = phoneNumber.length >= 8 && customerName.isNotBlank() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOperator == PaymentOperator.ORANGE_MONEY) Color(0xFFFF7900) else Color(0xFFFFCC00),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Valider & Payer ${"%,.0f".format(totalAmount)} XAF",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                2 -> {
                    // During USSD waiting, we allow instant simulate validation
                    Button(
                        onClick = {
                            // Fast-forward approval
                            statusMessage = "Validation manuelle USSD confirmée !"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AfriGold, contentColor = AfriBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("J'ai validé le code PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                3 -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AfriGold, contentColor = AfriBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Fermer & Voir ma commande", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (currentStep == 1) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Annuler")
                }
            }
        }
    )
}
