package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit,
    onLegalClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it / 2 }
            ) {
                Text(
                    text = "NboaShop",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Le marché près de chez vous",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Choisissez votre espace",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            RoleCard(
                title = "Espace Client",
                icon = Icons.Default.Person,
                delayMillis = 200,
                isVisible = isVisible,
                onClick = { onRoleSelected("Client") }
            )
            RoleCard(
                title = "Espace Vendeur",
                icon = Icons.Default.Storefront,
                delayMillis = 400,
                isVisible = isVisible,
                onClick = { onRoleSelected("Vendeur") }
            )
            RoleCard(
                title = "Espace Livreur",
                icon = Icons.Default.DeliveryDining,
                delayMillis = 600,
                isVisible = isVisible,
                onClick = { onRoleSelected("Livreur") }
            )
            RoleCard(
                title = "Espace Administrateur",
                icon = Icons.Default.AdminPanelSettings,
                delayMillis = 800,
                isVisible = isVisible,
                onClick = { onRoleSelected("Admin") }
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onLegalClick) {
                Text("Mentions Légales & Certificats", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: ImageVector,
    delayMillis: Int,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(delayMillis.toLong())
            cardVisible = true
        }
    }

    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
