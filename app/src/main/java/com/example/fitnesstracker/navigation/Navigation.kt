package com.example.fitnesstracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitnesstracker.ui.screens.home.HomeScreen
import com.example.fitnesstracker.ui.screens.profile.ProfileScreen
import com.example.fitnesstracker.ui.screens.training.TrainingScreen
import androidx.navigation.compose.NavHost

// Navigation host that is responsible for navigation between composables and connects routes from ScreenRoutes to composables
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = ScreenRoutes.HomeScreen.route) {
        composable(route = ScreenRoutes.HomeScreen.route) { HomeScreen() }
        composable(route = ScreenRoutes.ProfileScreen.route) { ProfileScreen() }
        composable(route = ScreenRoutes.TrainingScreen.route) { TrainingScreen() }
    }
}
// Bottom navigation bar composable that calls NavHost. This composable is called from MainActivity
@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // Specifies where the application starts on when launched
    val startDestination = ScreenRoutes.HomeScreen
    // Save the selected destination into a mutable object that creates an observable.
    // This value will persists thanks to the rememberSaveable and highlights the currently selected view on the bottom navigation bar
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                ScreenRoutes.entries.forEachIndexed { index, screenRoute ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = screenRoute.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                screenRoute.icon,
                                contentDescription = screenRoute.contentDescription
                            )
                        },
                        label = { Text(screenRoute.label) }
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navController, modifier = Modifier.padding(contentPadding))
    }
}