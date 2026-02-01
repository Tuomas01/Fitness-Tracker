package com.example.fitnesstracker.navigation

sealed class ScreenRoutes(val route: String) {
    object HomeScreen : ScreenRoutes("home_screen")
    object ProfileScreen : ScreenRoutes("profile_screen")
    object TrainingScreen : ScreenRoutes("training_screen")
}