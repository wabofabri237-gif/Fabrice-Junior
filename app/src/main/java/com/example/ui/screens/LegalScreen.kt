package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("À Propos", "Confidentialité", "Certificats & Sécurité", "CGU / CGV", "Mentions Légales")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("À Propos, Confidentialité & Certificats", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    when (selectedTab) {
                        0 -> AboutContent()
                        1 -> PrivacyContent()
                        2 -> CertificatesContent()
                        3 -> CGVContent()
                        4 -> LegalMentionsContent()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AboutContent() {
    LegalCard(
        title = "À Propos de NboaShop (AfriMarket)",
        icon = Icons.Default.Info,
        content = """
            1. Présentation de l'Application
            NboaShop est une application mobile de commerce électronique et de logistique urbaine de pointe, spécialement conçue pour dynamiser les échanges commerciaux et les circuits de livraison dans l'espace CEMAC et en Afrique.

            2. Fonctionnalités Clés
            - Catalogue de produits locaux et artisanaux de haute qualité.
            - Espaces professionnels sécurisés (Vendeur et Livreur) protégés par un code PIN chiffré à 4 chiffres.
            - Suivi des livraisons en temps réel avec chronologie visuelle et géolocalisation GPS.
            - Assistant intelligent propulsé par Gemini AI pour guider les utilisateurs.
            - Passerelles de paiement sécurisées (Mobile Money & cartes bancaires).

            3. Version & Architecture
            - Version actuelle : 2.7.0 (Production Stable)
            - Architecture : Jetpack Compose, Material Design 3, Room Database & Firebase Cloud Firestore.
            - Développé pour offrir rapidité, fiabilité et sécurité de bout en bout.
        """.trimIndent()
    )
}

@Composable
fun PrivacyContent() {
    LegalCard(
        title = "Politique de Confidentialité et Protection des Données (RGPD)",
        icon = Icons.Default.Lock,
        content = """
            1. Collecte des Données Personnelles
            NboaShop collecte uniquement les données strictement nécessaires au bon fonctionnement de la plateforme : nom, prénom, numéro de téléphone Mobile Money, adresse de livraison et géolocalisation GPS (avec votre consentement explicite).

            2. Utilisation des Données
            Vos données sont utilisées pour :
            - Faciliter la mise en relation sécurisée entre acheteurs, marchands et livreurs.
            - Traiter et valider les transactions financières.
            - Améliorer l'expérience utilisateur et personnaliser les recommandations grâce à l'assistant IA.

            3. Partage et Confidentialité
            Aucune donnée personnelle n'est cédée ou vendue à des tiers. Le partage de données est strictement restreint aux intervenants logistiques nécessaires (vendeur de la boutique concernée et livreur assigné à la course).

            4. Vos Droits d'Accès et de Suppression
            Vous disposez d'un droit total d'accès, de rectification et de suppression de vos données personnelles et adresses de livraison directement depuis l'écran 'Mon Profil' ou en contactant notre support technique.
        """.trimIndent()
    )
}

@Composable
fun CertificatesContent() {
    LegalCard(
        title = "Certificats de Sécurité & Conformité Officiels",
        icon = Icons.Default.Verified,
        content = """
            1. Certificat de Chiffrement SSL / TLS 256-bit
            - Statut : ACTIF & VÉRIFIÉ
            - Détail : Toutes les communications réseau et les requêtes vers le cloud Firestore sont protégées par un chiffrement fort TLS 1.3 garantissant l'inviolabilité des données en transit.

            2. Certificat de Sécurité des Paiements PCI-DSS
            - Statut : CONFORME
            - Détail : Les passerelles de paiement Mobile Money (MTN & Orange Money) respectent les normes internationales de sécurité PCI-DSS protégeant les informations sensibles des utilisateurs.

            3. Certificat d'Authentification PIN & Firebase Security
            - Statut : DÉPLOYÉ
            - Détail : Les accès aux espaces professionnels (Vendeur / Livreur) sont sécurisés par un système de hachage PIN à 4 chiffres et des règles de sécurité Firestore strictes bloquant tout accès non autorisé.

            4. Certificat de Conformité Commerciale CEMAC
            - Statut : HOMOLOGUÉ
            - Détail : NboaShop respecte les réglementations régionales en matière de commerce électronique, de transparence des prix en Francs CFA (XAF) et de traçabilité des livraisons transfrontalières.
        """.trimIndent()
    )
}

@Composable
fun CGVContent() {
    LegalCard(
        title = "Conditions Générales d'Utilisation et de Vente (CGU / CGV)",
        icon = Icons.Default.Gavel,
        content = """
            1. Objet et Champ d'Application
            Les présentes Conditions Générales régissent l'utilisation de l'application NboaShop et l'ensemble des transactions de commerce électronique effectuées entre les marchands (Vendeurs), les utilisateurs (Clients) et les prestataires logistiques (Livreurs).

            2. Inscription et Compte Professionnel
            L'accès aux espaces professionnels requiert l'enregistrement d'informations véridiques ainsi qu'un code PIN personnel sécurisé à 4 chiffres. L'utilisateur est seul responsable de la confidentialité de son code PIN.

            3. Commandes, Paiements et Tarifs
            Les prix des produits sont affichés en Francs CFA (XAF). Les paiements sont validés en toute sécurité via nos passerelles partenaires. NboaShop prélève une commission transparente sur les transactions validées.

            4. Livraison et Réception
            Les livraisons sont assurées par des partenaires livreurs géolocalisés. Le Client est tenu de vérifier l'état du produit à la livraison et de signaler toute anomalie dans un délai de 48 heures.
        """.trimIndent()
    )
}

@Composable
fun LegalMentionsContent() {
    LegalCard(
        title = "Mentions Légales & Intégrations API Officielles",
        icon = Icons.Default.Security,
        content = """
            1. Éditeur de l'Application
            NboaShop (AfriMarket) - Plateforme de commerce électronique et logistique urbaine.
            Contact support : support@nboashop.com | Tél : +237 600 000 000.

            2. Intégrations API et Services Tiers
            - Google Maps SDK & Location API : Utilisé pour la géolocalisation en temps réel.
            - Passerelles Mobile Money (MTN & Orange) : Intégration API sécurisée pour les règlements instantanés.
            - Gemini AI API : Assistant intelligent conversationnel.

            3. Propriété Intellectuelle
            L'ensemble des éléments graphiques, logos, codes sources et interfaces de l'application NboaShop sont protégés par les lois internationales sur la propriété intellectuelle.
        """.trimIndent()
    )
}

@Composable
fun LegalCard(title: String, icon: ImageVector, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }
    }
}
