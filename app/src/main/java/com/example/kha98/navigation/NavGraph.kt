package com.kha98.emograph.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kha98.emograph.ui.screens.input.InputScreen
import com.kha98.emograph.ui.screens.timeline.TimelineScreen

sealed class Screen(val route: String) {
    object Timeline : Screen("timeline")
    object Input : Screen("input?recordId={recordId}") {
        fun new() = "input?recordId="
        fun edit(id: String) = "input?recordId=$id"
    }
}

@Composable
fun EmoGraphNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Timeline.route) {
        composable(Screen.Timeline.route) {
            TimelineScreen(
                onNavigateToInput = { navController.navigate(Screen.Input.new()) },
                onNavigateToEdit = { id -> navController.navigate(Screen.Input.edit(id)) }
            )
        }

        composable(
            route = Screen.Input.route,
            arguments = listOf(
                navArgument("recordId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            InputScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
