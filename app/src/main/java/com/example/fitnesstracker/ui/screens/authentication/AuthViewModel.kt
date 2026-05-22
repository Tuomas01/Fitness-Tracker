package com.example.fitnesstracker.ui.screens.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.supabase.AuthRepository
import com.example.fitnesstracker.ui.screens.profile.User
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    //private val _password = MutableStateFlow("")
    //val password: StateFlow<String> = _password.asStateFlow()

    private val _otpToken = MutableStateFlow("")
    val otpToken: StateFlow<String> = _otpToken.asStateFlow()

    //private val _passwordlessAuth = MutableStateFlow(true)
    //val passwordlessAuth: StateFlow<Boolean> = _passwordlessAuth.asStateFlow()
    private val _sessionState = MutableStateFlow<SessionStatus?>(null)
    val sessionState: StateFlow<SessionStatus?> = _sessionState.asStateFlow()

    // Gets user from the session when the view model is initialized
    init {
        getUserFromSession()
        //getSessionStatus()
    }

    // Updates the email text field value
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    /*fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }*/

    // Updates the token text field value
    fun updateToken(token: String) {
        _otpToken.value = token
    }

    /*fun changeAuthMethod() {
        _passwordlessAuth.value = !_passwordlessAuth.value
    }*/

    // Calls the auth repository authenticateWithOtp function and uses the email from email text field as a value if it isn't empty
    fun authenticateWithOtp() {
        if (_email.value.isNotEmpty()) {
            viewModelScope.launch {
                authRepository.authenticateWithOtp(_email.value)
            }
        } else {
            Log.d("AuthVM", "Email has not been filled in")
        }
    }

    // Calls the auth repository verifyOtp function using the email and otp text field values if they are not empty
    suspend fun verifyOtp(): Boolean {
        if (_email.value.isNotEmpty() && _otpToken.value.isNotEmpty()) {
            try {
                // Returns true if verifying was successful and false if it failed
                val authenticated = authRepository.verifyOtp(
                    userEmail = _email.value,
                    otp = _otpToken.value
                )
                Log.d("AuthVM", "verifyOtp() test: $authenticated")
                return authenticated
            } catch (e: Exception) {
                Log.d("AuthVM", "verifyOtp() error: $e")
                return false
            }
        } else {
            Log.d("AuthVM", "Email or OTP token has not been filled in properly")
            return false
        }
    }

    fun getUserFromSession() {
        // Auth repository returns user info from the session's user data if a session is active and null if a session was not found
        val user = authRepository.retrieveUserFromSession()
        Log.d("AuthVM", "getUserFromSession() test: $user")
    }

    fun getSessionStatus() {
        try {
            val sessionStatus = authRepository.retrieveSessionStatus()
            _sessionState.value = sessionStatus
            Log.d("AuthVM", "getSessionStatus(): $sessionStatus")
        } catch (e: Exception) {
            Log.d("AuthVM", "getSessionStatus() error: $e")
        }
    }

    // Calls the auth repository anonymousSignIn function to log the user in anonymously
    // Doesn't return anything but if it was successful, a new authenticated session will be created
    fun anonymousSignIn() {
        try {
            viewModelScope.launch {
                val response = authRepository.anonymousSignIn()
                Log.d("AuthVM", "anonymousSignIn() test: $response")
            }
        } catch (e: Exception) {
            Log.d("AuthVM", "anonymousSignIn() error: $e")
        }
    }

    // Removes the active session
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _email.value = ""
            } catch (e: Exception) {
                Log.d("AuthVM", "signOut() error: $e")
            }
        }
        authRepository.retrieveUserFromSession()
    }
}