package com.example.fitnesstracker.supabase

import android.util.Log
import androidx.compose.runtime.collectAsState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import javax.inject.Inject

// AuthRepository interface that is used when the app wants to interact with supabase auth
interface AuthRepository {
    suspend fun authenticateWithOtp(userEmail: String)
    suspend fun verifyOtp(userEmail: String, otp: String): Boolean
    fun retrieveUserFromSession(): UserInfo?
    fun retrieveSessionStatus(): SessionStatus?
    suspend fun signOut()
    suspend fun updateUserEmail(userEmail: String): Boolean
    suspend fun anonymousSignIn()
}

// The implementation for AuthRepository
// All the auth related functions will happen here
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
) : AuthRepository {

    // Signs in the user using an OTP code that will be sent to the user's email
    // Parameter userEmail is user's email taken from the email text field in Authentication.kt
    override suspend fun authenticateWithOtp(userEmail: String) {
        try {
            // Signs in the user using OTP
            // If user doesn't have an account with the given email, registers the given email and signs in
            val response = auth.signInWith(OTP) {
                email = userEmail
            }
            Log.d("AuthRepo", "authenticateWithOtp() test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "authenticateWithOtp() error: $e")
        }
    }

    // Verify whether the given email and token are valid.
    // If so, the user will be logged in and a new valid session will be created
    override suspend fun verifyOtp(userEmail: String, otp: String): Boolean {
        try {
            val response = auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = userEmail,
                token = otp
            )
            Log.d("AuthRepo", "verifyOtp() test: $response")
            return true
        } catch (e: Exception) {
            Log.d("AuthRepo", "verifyOtp() error: $e")
            return false
        }
    }

    // Retrieves the current active session or null if no session is active
    // Returns the user's id from the session data or null if no session was found
    override fun retrieveUserFromSession(): UserInfo? {
        val session = auth.currentSessionOrNull()
        Log.d("AuthRepo", "retrieveSession() test: $session")
        if (session !== null) {
            return session.user
        } else {
            return null
        }
    }

    // Retrieves the session status.
    // Gives more detailed information about the state of session.
    // For example, the session status can be used to check if the application is busy loading the session from storage
    // and that way the application can display a loading screen to the user.
    // Returns the session status or Null if no session was found
    override fun retrieveSessionStatus(): SessionStatus? {
        try {
            val sessionStatus = auth.sessionStatus.value
            Log.d("AuthRepo", "retrieveSessionState() test: $sessionStatus")
            return sessionStatus
        } catch (e: Exception) {
            Log.d("AuthRepo", "retrieveSessionStatus() error: $e")
            return null
        }
    }

    // Updates the user's email with given email
    override suspend fun updateUserEmail(userEmail: String): Boolean {
        try {
            val user = auth.updateUser {
                email = userEmail
            }
            Log.d("AuthRepo", "updateUserEmail() test: $user")
            return true
        } catch (e: Exception) {
            Log.d("AuthRepo", "updateUserEmail() error: $e")
            return false
        }
    }

    // Signs the user in anonymously so that the user can access the app without signing in
    // Creates a new authenticated session
    override suspend fun anonymousSignIn() {
        try {
            val response = auth.signInAnonymously()
            Log.d("AuthRepo", "anonymousSignIn() test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "anonymousSignIn() error: $e")
        }
    }

    // Signs the user out
    override suspend fun signOut() {
        try {
            // Signs out the user from all sessions
            val response = auth.signOut(SignOutScope.GLOBAL)
            Log.d("AuthRepo", "signOut() test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "signOut() error: $e")
        }
    }
}

// Create a new Module and scope it to the whole application using the SingletonComponent
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    // Binds the AuthRepository so that it can be injected into a constructor
    // Takes the implementation of the repository as a parameter
    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}