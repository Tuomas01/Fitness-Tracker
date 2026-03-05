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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _sessionState = MutableStateFlow<SessionStatus?>(null)
    val sessionState: StateFlow<SessionStatus?> = _sessionState.asStateFlow()

    init {
        getSession()
        //getSessionStatus()
    }

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    /*fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }*/

    fun updateToken(token: String) {
        _otpToken.value = token
    }

    /*fun changeAuthMethod() {
        _passwordlessAuth.value = !_passwordlessAuth.value
    }*/

    fun authenticateWithOtp() {
        if (_email.value.isNotEmpty()) {
            viewModelScope.launch {
                authRepository.authenticateWithOtp(_email.value)
            }
        } else {
            Log.d("AuthVM", "Email has not been filled in")
        }
    }

    fun verifyOtp() {
        if (_email.value.isNotEmpty() && _otpToken.value.isNotEmpty()) {
            try {
                viewModelScope.launch {
                    authRepository.verifyOtp(
                        userEmail = _email.value,
                        otp = _otpToken.value
                    )
                }
                _isLoggedIn.value = true
            } catch (e: Exception) {
                Log.d("AuthVM", "verifyOtp() error: $e")
                _isLoggedIn.value = false
            }
        } else {
            Log.d("AuthVM", "Email and OTP token have not been filled in")
            _isLoggedIn.value = false
        }
    }

    fun getSession() {
        // Auth repository returns email from the session's user data if a session is active and null if a session was not found
        val session = authRepository.retrieveSession()
        Log.d("AuthVM", "getSession() test: $session")
        // Assigns a boolean value to the _isLoggedIn variable based on if there is an active session or not
    }

    fun getSessionStatus() {
        try {
            val sessionStatus = authRepository.retrieveSessionStatus()
            _sessionState.value = sessionStatus
            Log.d("AuthVm", "getSessionStatus(): $sessionStatus")
        } catch (e: Exception) {
            Log.d("AuthVm", "getSessionStatus() error: $e")
        }
    }

    // Removes the active session
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
        authRepository.retrieveSession()
    }
}