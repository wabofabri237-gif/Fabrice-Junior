package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LegalScreen

@Composable
fun AfriMarketNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home/Client") {
        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role -> 
                    if (role == "Client") {
                        navController.navigate("home/$role")
                    } else {
                        navController.navigate("login/$role")
                    }
                },
                onLegalClick = {
                    navController.navigate("legal")
                }
            )
        }
        composable("login/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "Client"
            LoginScreen(
                role = role,
                onLoginSuccess = {
                    navController.navigate("home/$role") {
                        popUpTo("role_selection")
                    }
                },
                onBack = { navController.popBackStack() }
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
