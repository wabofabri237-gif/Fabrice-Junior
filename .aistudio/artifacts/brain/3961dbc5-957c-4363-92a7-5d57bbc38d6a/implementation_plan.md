# Plan de Mise en Œuvre Global - NboaShop Marketplace CEMAC

Ce plan définit la feuille de route complète pour transformer NboaShop en une plateforme e-commerce africaine prête pour la production, intégrant un backend persistant Firebase Firestore sécurisé, l'authentification Google via Credential Manager, et un système de paiement robuste combinant Paiement à la Livraison (Cash on Delivery) et Virement Bancaire Local.

### User Review & Critical Decisions

> [!IMPORTANT]
> **Décisions validées avec l'utilisateur :**
> - **Objectif principal :** Déploiement complet en production avec backend réel et gestion opérationnelle des flux marchands.
> - **Infrastructure backend :** Firebase Cloud Firestore avec règles de sécurité durcies (*Zero-Trust*) et Authentification Google native via Android Jetpack `CredentialManager`.
> - **Modes de paiement retenus :** Paiement à la livraison (Cash on Delivery / Espèces au livreur avec validation par code OTP/PIN) et Virement bancaire local (téléversement du bordereau/preuve de virement et validation administrative/vendeur).
> - **Compatibilité conservée :** Les modèles de données préserveront l'interopérabilité avec les options Mobile Money CEMAC existantes en tant que compléments ultérieurs.

---

### 1. Overview & Core Concept

- **Ce que fait l'application :** NboaShop est une place de marché mobile taillée pour la zone CEMAC (Cameroun, Gabon, Congo, Tchad, Centrafrique, Guinée Équatoriale), connectant acheteurs, vendeurs professionnels et livreurs avec un suivi en temps réel des commandes, un catalogue dynamique et une gestion logistique de bout en bout.
- **Public cible :**
  - **Acheteurs :** Utilisateurs mobiles cherchant des produits locaux et importés avec garantie de réception avant paiement ou virement direct sécurisé.
  - **Vendeurs (Boutiques & Marchands Pro) :** Gestionnaires de catalogue, suivi des commandes entrantes, gestion des stocks et abonnements pro.
  - **Livreurs / Dispatchers :** Validation des livraisons sur le terrain, encaissement des espèces à la livraison et saisie du code de confirmation.
  - **Administrateurs :** Surveillance des flux, vérification des preuves de virements bancaires et analytics de conversion.
- **Valeur clé :** Zéro friction de paiement grâce au paiement à la livraison sécurisé par code de validation, combiné à la traçabilité bancaire locale et à la persistance cloud en temps réel.

---

### 2. User Experience & Visual Design

#### Parcours Utilisateurs Clés

1. **Parcours Acheteur (Catalogue -> Commande -> Paiement) :**
   - L'acheteur explore les catégories et articles avec recherche instantanée et filtres de disponibilité.
   - Ajout au panier et passage en caisse : sélection de l'adresse de livraison (quartier, ville, repère GPS/contact).
   - Sélection du mode de paiement :
     - *Option A - Espèces à la livraison :* Génération d'un code OTP sécurisé à 4 chiffres à présenter au livreur lors de la réception du colis.
     - *Option B - Virement bancaire local :* Affichage des coordonnées bancaires agréées (RIB/IBAN CEMAC), téléchargement de la photo du récépissé et mise en attente de vérification.
   - Suivi en temps réel de la commande (*En attente*, *Confirmée*, *En cours de livraison*, *Livrée & Payée*).

2. **Parcours Vendeur (Gestion & Réapprovisionnement) :**
   - Tableau de bord des ventes et gestion de stock avec notifications en temps réel des nouvelles commandes.
   - Validation des commandes payées par virement après confirmation bancaire.

3. **Parcours Livreur (Dispatch & Encaissement) :**
   - Liste des courses assignées avec coordonnées géographiques et bouton d'appel direct du destinataire.
   - Remise du colis contre encaissement des espèces et saisie du code de validation client pour clore la course.

#### Identité Visuelle & Direction Artistique

- **Ambiance :** Moderne, dynamique et ancrée dans le commerce africain contemporain (alliant chaleur visuelle et rigueur bancaire).
- **Palette de Couleurs Material 3 :**
  - *Primaire :* Vert Émeraude Profond (`#006D44` / Dark: `#65DBA0`) symbolisant la prospérité, la fiabilité et le dynamisme économique.
  - *Secondaire & Accent :* Or Ambré Chaud (`#C68A00` / `#FFBA35`) pour les alertes de commandes, badges Pro et statuts VIP.
  - *Surfaces & Fonds :* Fond blanc cassé doux (`#FBFDF8`) en mode clair, graphite profond (`#111411`) en mode sombre, avec cartes surélevées à coins adoucis (16.dp).
- **Typographie :**
  - Titres et Chiffres : Police moderne sans-serif géométrique, avec mise en avant percutante des montants en Francs CFA (XAF).
  - Corps de texte : Lisibilité maximale sur tous écrans avec contrastes WCAG AAA.
- **États Interactifs & Micro-animations :**
  - Transitions fluides via `AnimatedVisibility` lors de la bascule entre étapes de paiement.
  - Cartes d'articles avec micro-effets de pressions haptiques et badges d'état distinctifs.

---

### 3. Key Product Decisions & Trade-Offs

#### Décision 1 : Authentification Google native via Credential Manager
- **Approche choisie :** Utilisation exclusive de l'API moderne Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) associée à Firebase Auth.
- **Pourquoi :** Connexion en un clic, zéro mot de passe à mémoriser pour l'utilisateur, sécurité cryptographique maximale et respect strict des règles de sécurité Firebase.
- **Alternatives écartées :** Connexion anonyme (non supportée en production et vulnérable aux pertes de session) et formulaires e-mail/mot de passe traditionnels (taux d'abandon trop élevé sur mobile).

#### Décision 2 : Double Voie de Paiement : Espèces à la livraison + Virement Bancaire Local
- **Approche choisie :** 
  - *Espèces à la livraison :* Intégration d'un mécanisme de validation par code secret client (OTP 4 chiffres). Le livreur ne peut marquer la commande comme livrée et encaissée sans saisir ce code dans son application.
  - *Virement bancaire :* Système d'instruction avec affichage clair du RIB/Banque locale (Afriland First Bank, UBA, Société Générale, BGFI), génération d'une référence unique de virement, et champ de téléversement de la preuve de transaction.
- **Pourquoi :** Adapté à 100% à la réalité du terrain d'Afrique Centrale où la majorité des transactions B2C s'effectuent à la livraison avec vérification physique du produit, tout en ouvrant le canal B2B et gros paniers aux virements interbancaires.
- **Alternatives écartées :** Exclusivité des passerelles tierces Mobile Money en phase 1 (évite les blocages liés aux KYC marchands complexes et aux pannes d'APIs opérateurs).

#### Décision 3 : Architecture Zero-Trust sur Firestore
- **Approche choisie :** Collections isolées avec validation de schéma (`isValidOrder()`, `isValidUser()`), permissions granulaires par rôles (`request.auth.uid == resource.data.clientId` ou rôle `seller`/`admin`/`courier`), et tests d'intégration locaux automatisés.
- **Pourquoi :** Empêche tout utilisateur malveillant de modifier le statut de paiement, les montants ou les adresses d'un tiers directement via le SDK.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        NboaShop Android Client                         │
│                    (Jetpack Compose & Material 3)                      │
└───────┬────────────────────────────────┬───────────────────────────────┘
        │                                │
        ▼                                ▼
┌───────────────────────┐        ┌───────────────────────────────────────┐
│ Jetpack CredentialMgr │        │          UI & Navigation Layer        │
│   (Google Sign-In)    │        │  - Client / Vendeur / Livreur Views   │
└───────┬───────────────┘        │  - Checkout (COD & Virement)          │
        │                        └───────────────────┬───────────────────┘
        ▼                                            │
┌───────────────────────┐                            ▼
│     Firebase Auth     │                ┌───────────────────────┐
│ (Tokens & User Session│◄───────────────┤   App ViewModels &    │
└───────────────────────┘                │   State Management    │
                                         └───────────┬───────────┘
                                                     │
                                                     ▼
                                         ┌───────────────────────┐
                                         │   Repository Layer    │
                                         │ (Order, Product, Auth)│
                                         └───────────┬───────────┘
                                                     │
                                                     ▼
                                         ┌───────────────────────┐
                                         │  Firebase Firestore   │
                                         │ (Rules Zero-Trust DB) │
                                         └───────────────────────┘
```

#### Modèle de Données Firestore

1. **`users/{userId}` :**
   - `id: String` (UID Firebase)
   - `email: String`, `displayName: String`, `phone: String`
   - `role: String` ("CLIENT", "SELLER", "COURIER", "ADMIN")
   - `city: String`, `country: String` ("CAMEROON", "GABON", etc.)
   - `createdAt: Timestamp`, `updatedAt: Timestamp`

2. **`orders/{orderId}` :**
   - `id: String`
   - `clientId: String`, `clientName: String`, `clientPhone: String`
   - `sellerId: String`
   - `courierId: String?`
   - `items: List<OrderItem>` (`productId`, `title`, `unitPrice`, `quantity`)
   - `totalAmount: Double` (en XAF)
   - `paymentMethod: String` ("CASH_ON_DELIVERY", "BANK_TRANSFER")
   - `paymentStatus: String` ("PENDING", "VERIFIED_BY_ADMIN", "COLLECTED_BY_COURIER")
   - `deliveryAddress: Map` (`city`, `neighborhood`, `landmark`, `phone`)
   - `deliveryOtp: String` (code à 4 chiffres généré pour validation COD)
   - `bankTransferProof: Map?` (`bankName`, `referenceCode`, `receiptUrl`, `submittedAt`)
   - `status: String` ("PENDING", "CONFIRMED", "PREPARING", "IN_TRANSIT", "DELIVERED", "CANCELLED")
   - `createdAt: Timestamp`, `updatedAt: Timestamp`

3. **`products/{productId}` :**
   - `id: String`, `sellerId: String`
   - `title: String`, `description: String`, `category: String`
   - `price: Double`, `stockQuantity: Long`, `imageUrl: String`
   - `isAvailable: Boolean`
   - `createdAt: Timestamp`, `updatedAt: Timestamp`

4. **`bank_accounts/{accountId}` (Lecture publique authentifiée, écriture admin) :**
   - `bankName: String` (ex: "Afriland First Bank")
   - `accountNumber: String`, `accountHolder: String`, `ibanOrRib: String`
   - `swiftCode: String`, `country: String`

---

### 5. Phases d'Exécution & Livrables

- **Phase 1 : Provisioning Firebase & Authentification Sécurisée**
  - Provisioning de Firestore et Firebase Auth via l'outil système.
  - Implémentation du composant d'authentification Google avec `CredentialManager`.
  - Auth gating racine de l'application (`MainActivity` et `AfriMarketNavGraph`).

- **Phase 2 : Schéma Firestore, Règles Zero-Trust & Tests**
  - Écriture de `firebase-blueprint.json` et `firestore.rules`.
  - Tests unitaires et d'intégration Robolectric (`FirestoreEmulatorTestBase`).
  - Déploiement des règles vers le projet Firebase distant (`deploy_firebase`).

- **Phase 3 : Gestion Complète des Commandes & Flux de Paiement (COD & Virement)**
  - Écrans de sélection du mode de paiement dans le panier.
  - Écran de paiement à la livraison avec génération et affichage de l'OTP de validation.
  - Écran de virement bancaire local avec coordonnées bancaires régionales et saisie de la référence de paiement.
  - Interface dédiée pour livreur permettant la validation physique du colis par saisie de l'OTP client.

- **Phase 4 : Validation Globale, Compilation & Tests**
  - Tests automatisés des flux de données et tests Robolectric.
  - Vérification de l'absence de régressions avec `compile_applet`.
