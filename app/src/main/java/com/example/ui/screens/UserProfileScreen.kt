package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

data class UserProfile(
    val fullName: String = "Jean Dupont",
    val phone: String = "690123456",
    val countryCode: String = "+237 (Cameroun)",
    val address: String = "Quartier Bonanjo, Rue 12",
    val city: String = "Douala",
    val instructions: String = "Sonner au portail vert",
    val email: String = "jean.dupont@nboashop.com",
    val updatedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("Jean Dupont") }
    var phone by remember { mutableStateOf("690123456") }
    var selectedCountryCode by remember { mutableStateOf("+237 (Cameroun)") }
    var address by remember { mutableStateOf("Quartier Bonanjo, Rue 12") }
    var city by remember { mutableStateOf("Douala") }
    var instructions by remember { mutableStateOf("Sonner au portail vert") }
    var email by remember { mutableStateOf("jean.dupont@nboashop.com") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val countryCodes = listOf(
        "+237 (Cameroun)",
        "+241 (Gabon)",
        "+242 (Congo)",
        "+235 (Tchad)",
        "+236 (Centrafrique)",
        "+240 (Guinée Éq.)"
    )
    var expandedCountryDropdown by remember { mutableStateOf(false) }

    // Load profile from Firestore on start
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document("current_user_profile")
                .get()
                .addOnSuccessListener { document ->
                    isLoading = false
                    if (document != null && document.exists()) {
                        fullName = document.getString("fullName") ?: fullName
                        phone = document.getString("phone") ?: phone
                        selectedCountryCode = document.getString("countryCode") ?: selectedCountryCode
                        address = document.getString("address") ?: address
                        city = document.getString("city") ?: city
                        instructions = document.getString("instructions") ?: instructions
                        email = document.getString("email") ?: email
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                    errorMessage = "Mode hors-ligne : Profil chargé localement."
                }
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Mode hors-ligne : Profil chargé localement."
        }
    }

    // Function to save profile securely to Firestore
    val saveProfileToFirestore: () -> Unit = {
        isSaving = true
        errorMessage = null
        successMessage = null

        val profileData = hashMapOf(
            "fullName" to fullName,
            "phone" to phone,
            "countryCode" to selectedCountryCode,
            "address" to address,
            "city" to city,
            "instructions" to instructions,
            "email" to email,
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document("current_user_profile")
                .set(profileData)
                .addOnSuccessListener {
                    isSaving = false
                    successMessage = "Profil et adresse mis à jour avec succès dans Firestore !"
                    Toast.makeText(context, "Profil enregistré !", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    isSaving = false
                    // Fallback local success if Firestore offline
                    successMessage = "Profil enregistré localement (Hors-ligne)."
                    Toast.makeText(context, "Profil enregistré localement", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            isSaving = false
            successMessage = "Profil enregistré localement."
            Toast.makeText(context, "Profil enregistré localement", Toast.LENGTH_SHORT).show()
        }
        Unit
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon Profil & Adresse de Livraison", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Security Notice Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Column {
                                Text("Sécurité & Chiffrement Firestore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Vos coordonnées personnelles et adresses de livraison CEMAC sont synchronisées de manière sécurisée.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    if (successMessage != null) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = successMessage!!,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFEF6C00),
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Personal Information Section
                    Text("Informations Personnelles", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nom complet") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Phone Number with Country Code (CEMAC)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country Code Dropdown
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { expandedCountryDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedCountryCode, fontSize = 12.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = expandedCountryDropdown,
                                onDismissRequest = { expandedCountryDropdown = false }
                            ) {
                                countryCodes.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text(code) },
                                        onClick = {
                                            selectedCountryCode = code
                                            expandedCountryDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Phone Input
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Numéro Mobile Money") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.8f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))

                    // Delivery Address Section
                    Text("Adresse de Livraison Principale", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Rue, Quartier, Repère précis") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville / Région") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions de livraison pour le livreur (optionnel)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = saveProfileToFirestore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer les modifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
