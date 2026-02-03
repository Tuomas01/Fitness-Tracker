package com.example.fitnesstracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class ScreenRoutes(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    TrainingScreen("training_screen", "Training", Icons.Default.FitnessCenter, "Training screen"),
    HomeScreen("home_screen", "Home", Icons.Default.Home, "Home screen"),
    ProfileScreen("profile_screen", "Profile", Icons.Default.AccountCircle, "Profile screen")
}