package com.example.fitnesstracker.navigation

import android.content.Context
import androidx.compose.foundation.layout.Column
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
import androidx.navigation.navigation
import com.example.fitnesstracker.ui.screens.authentication.AuthenticationScreen
import com.example.fitnesstracker.ui.screens.authentication.InitializingScreen
import com.example.fitnesstracker.ui.screens.profile.UpdateUserScreen

// Navigation host that is responsible for navigation between composables and connects routes from ScreenRoutes to composables
@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    NavHost(navController, startDestination = "main") {
        // Nested navigation graph for authentication
        navigation(
            startDestination = AuthScreenRoute.AuthScreen.route,
            route = "auth"
        ) {
            composable(route = AuthScreenRoute.AuthScreen.route) { AuthenticationScreen() }
            composable(route = AuthScreenRoute.InitializingScreen.route) { InitializingScreen() }
        }

        navigation(
            startDestination = ScreenRoutes.HomeScreen.route,
            route = "main"
        ) {
            composable(route = ScreenRoutes.HomeScreen.route) { HomeScreen() }
            composable(route = ScreenRoutes.ProfileScreen.route) {
                ProfileScreen(
                    navigateToUpdateUser = {
                        navController.navigate(route = ScreenRoutes.UpdateUserScreen.route)
                    },
                    navigateToHome = {
                        navController.navigate(route = ScreenRoutes.HomeScreen.route)
                    },
                    clearBackStack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(route = ScreenRoutes.TrainingScreen.route) { TrainingScreen() }
            composable(route = ScreenRoutes.UpdateUserScreen.route) {
                UpdateUserScreen(
                    onNavigate = {
                        navController.navigate(route = ScreenRoutes.ProfileScreen.route)
                    }
                )
            }
        }
    }
}

// Bottom navigation bar composable that calls NavHost. This composable is called from MainActivity
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
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
                    // Excludes the updateUser screen from the bottom bar
                    if (screenRoute.route !== ScreenRoutes.UpdateUserScreen.route) {
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
        }
    ) { contentPadding ->
        // A column for AppNavHost composable to set padding on each page using the contentPadding.calculateBottomPadding() function.
        // This makes it so that the bottom navigation bar doesn't overlap with the content on the page
        Column(
            modifier = Modifier
                .padding(bottom = contentPadding.calculateBottomPadding())
        ) {
            AppNavHost(navController)
        }
    }
}