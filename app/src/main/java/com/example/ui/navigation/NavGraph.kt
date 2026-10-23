package com.example.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.RoleSelectionScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun AfriMarketNavGraph() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        LoginScreen(
            role = "Client",
            onLoginSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onBack = {}
        )
    } else {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "home/Client") {
            composable("role_selection") {
                RoleSelectionScreen(
                    onRoleSelected = { role ->
                        navController.navigate("home/$role")
                    },
                    onLegalClick = {
                        navController.navigate("legal")
                    }
                )
            }
            composable("home/{role}") { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "Client"
                HomeScreen(role = role)
            }
            composable("legal") {
                LegalScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
