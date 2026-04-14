package com.example.emograph.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emograph.ui.screens.auth.AuthScreen
import com.example.emograph.ui.screens.input.InputScreen
import com.example.emograph.ui.screens.timeline.TimelineScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Timeline : Screen("timeline")
    object Input : Screen("input")
}

@Composable
fun EmoGraphNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Timeline.route
    } else {
        Screen.Auth.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Timeline.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Timeline.route) {
            TimelineScreen(
                onNavigateToInput = {
                    navController.navigate(Screen.Input.route)
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Timeline.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Input.route) {
            InputScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
