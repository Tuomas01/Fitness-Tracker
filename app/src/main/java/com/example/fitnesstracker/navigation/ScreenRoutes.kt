package com.example.fitnesstracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.ui.graphics.vector.ImageVector

// Routes are specified here
enum class ScreenRoutes(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    TrainingScreen("training_screen", "Training", Icons.Default.FitnessCenter, "Training screen"),
    TrainingPlanScreen("training_plan_screen", "Training plan", Icons.Default.CalendarMonth, "Training plan screen"),
    HomeScreen("home_screen", "Home", Icons.Default.Home, "Home screen"),
    ProfileScreen("profile_screen", "Profile", Icons.Default.AccountCircle, "Profile screen"),
    UpdateUserScreen("update_user_screen","Update user", Icons.Default.ManageAccounts, "Update user screen")
}

enum class AuthScreenRoute(
    val route: String,
    val label: String,
    val contentDescription: String
) {
    AuthScreen("authentication_screen", "Authentication", "Authentication screen"),
    InitializingScreen("initializing_screen", "Initializing", "Initializing screen")
}