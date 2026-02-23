package com.example.fitnesstracker.ui.screens.authentication

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel() : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _otpToken = MutableStateFlow("")
    val otpToken: StateFlow<String> = _otpToken.asStateFlow()

    private val _passwordlessAuth = MutableStateFlow(true)
    val passwordlessAuth: StateFlow<Boolean> = _passwordlessAuth.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    fun updateToken(token: String) {
        _otpToken.value = token
    }

    fun changeAuthMethod() {
        _passwordlessAuth.value = !_passwordlessAuth.value
    }

    suspend fun authenticateUser() {
        /*if (_passwordlessAuth.value) {
            supabase.auth.signInWith(OTP) {
                email = _email.value
            }
        } else {

        }*/
        _isLoggedIn.value = true
    }

    fun signOut() {
        _isLoggedIn.value = false
    }
}