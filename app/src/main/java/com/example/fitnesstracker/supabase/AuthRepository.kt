package com.example.fitnesstracker.supabase

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.user.UserInfo
import javax.inject.Inject

// AuthRepository interface that is used when the app wants to interact with supabase auth
interface AuthRepository {
    suspend fun authenticateWithOtp(userEmail: String)
    suspend fun verifyOtp(userEmail: String, otp: String)
    fun retrieveSession(): String?
    suspend fun signOut()
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
            Log.d("AuthRepo", "authenticateWithOtp test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "authenticateWithOtp error: $e")
        }
    }

    // Verify whether the given email and token are valid.
    // If so, the user will be logged in and a new valid session will be created
    override suspend fun verifyOtp(userEmail: String, otp: String) {
        try {
            val response = auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = userEmail,
                token = otp
            )
            Log.d("AuthRepo", "verifyOtp test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "verifyOtp error: $e")
        }
    }

    // Retrieves the current active session or null if no session is active
    // Returns the user's email from the session data or null if no session was found
    override fun retrieveSession(): String? {
        val session = auth.currentSessionOrNull()
        Log.d("AuthRepo", "retrieveSession test: $session")
        if (session !== null) {
            return session.user?.email
        } else {
            return null
        }
    }

    // Signs the user out
    override suspend fun signOut() {
        try {
            val response = auth.signOut()
            Log.d("AuthRepo", "signOut test: $response")
        } catch (e: Exception) {
            Log.d("AuthRepo", "signOut error: $e")
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