package com.example.fitnesstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.navigation.AppNavigation
import com.example.fitnesstracker.ui.screens.authentication.AuthViewModel
import com.example.fitnesstracker.ui.screens.authentication.AuthenticationScreen
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Application's navigation controller, which is passed as an argument to the AppBottomNavigationBar
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val loggedIn by authViewModel.isLoggedIn.collectAsState()
            FitnessTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Application's bottom navigation bar that shows on all views
                    // The other views are accessible through NavHost which the AppBottomNavigationBar composable calls
                    if (loggedIn) {
                        AppNavigation(Modifier, navController)
                    } else {
                        AuthenticationScreen()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ComposablePreview() {
    AppNavigation(Modifier, navController = rememberNavController())
}
