package com.example.fitnesstracker.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import com.example.fitnesstracker.ui.screens.home.HomeScreen
import com.example.fitnesstracker.ui.screens.profile.ProfileScreen
import com.example.fitnesstracker.ui.screens.training.TrainingScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ScreenRoutes.HomeScreen.route) {
        composable(route = ScreenRoutes.HomeScreen.route) { HomeScreen() }
        composable(route = ScreenRoutes.ProfileScreen.route) { ProfileScreen() }
        composable(route = ScreenRoutes.TrainingScreen.route) { TrainingScreen() }
    }
}