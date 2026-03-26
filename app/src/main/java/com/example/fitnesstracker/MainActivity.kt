package com.example.fitnesstracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.navigation.AppNavigation
import com.example.fitnesstracker.navigation.ScreenRoutes
import com.example.fitnesstracker.ui.screens.authentication.AuthViewModel
import com.example.fitnesstracker.ui.screens.authentication.AuthenticationScreen
import com.example.fitnesstracker.ui.screens.authentication.InitializingScreen
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Application's navigation controller, which is passed as an argument to the AppBottomNavigationBar
            val navController = rememberNavController()

            // Uses the supabaseClient to get the sessionStatus as state
            // which gives more detailed information about the status of the session.
            // This allows the application to conditionally render the correct screen for the user based on the session status
            // Ideally this variable would be inside the viewmodel and the value gotten from the authRepository
            // but I couldn't get it to work properly using
            // authRepo function retrieveSessionStatus() and authViewModel function getSessionStatus()
            val sessionStatus by supabaseClient.auth.sessionStatus.collectAsState()
            Log.d("MainActivity", "testing sessionStatus $sessionStatus")

            FitnessTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Check the session status and display different screens based on the session value
                    when (sessionStatus) {
                        // User is logged in
                        is SessionStatus.Authenticated -> {
                            // Application's bottom navigation bar that shows on all views
                            // The other views are accessible through NavHost which the AppNavigation composable calls
                            AppNavigation(Modifier, navController)
                            Log.d("MainActivity", "Session has been authenticated")
                        }

                        // The application is loading session from storage
                        is SessionStatus.Initializing -> {
                            // Show the initializing screen while retrieving the session
                            InitializingScreen()
                            Log.d("MainActivity", "Initializing session")
                        }

                        // There was an error retrieving the session
                        is SessionStatus.RefreshFailure -> {
                            AuthenticationScreen()
                            Log.d("MainActivity", "Refreshing session failed")
                        }

                        // User hasn't been logged in
                        is SessionStatus.NotAuthenticated -> {
                            // Show the authentication screen instead if no active session was found
                            AuthenticationScreen()
                            Log.d("MainActivity", "No session found, user has not been signed in")
                        }

                        /*else -> {
                            AuthenticationScreen(context = this)
                            Log.d("MainActivity", "None of the above")
                        }*/
                    }
                }
            }
        }
    }
}