package com.example.fitnesstracker.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.supabase.AuthRepository
import com.example.fitnesstracker.supabase.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Handles all the data related to the user's information and the profile page
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Uses stateflow to persist and handle user information
    // UserState is a data class that has all the user information fields

    // This variable is only accessible by the viewmodel
    private val _userState = MutableStateFlow(User())

    // The ProfileTextFields composable uses this variable for default and updated values for the text fields
    val userState: StateFlow<User> = _userState.asStateFlow()

    private val _loggedInEmail = MutableStateFlow("")
    val loggedInEmail: StateFlow<String> = _loggedInEmail.asStateFlow()

    init {
        viewModelScope.launch {
            getSessionEmail()
            getUser(_loggedInEmail.value)
        }
    }

    fun getUsers() {
        viewModelScope.launch {
            userRepository.getUsers()
        }
    }

    fun getUser(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUser(email)
            Log.d("ProfileVM", "Testing getUser: $user")
            _userState.emit(user)
        }
    }

    fun getSessionEmail() {
        viewModelScope.launch {
            val sessionEmail = authRepository.retrieveSession()
            Log.d("ProfileVM", "getSessionEmail test: $sessionEmail")
            if (sessionEmail !== null) {
                _loggedInEmail.value = sessionEmail
            } else {
                Log.d("ProfileVM", "getSessionEmail: no active session found")
            }
        }
    }

    // Functions for updating userState, which in return updates the text fields in real time
    // Functions for clearing the text field when user taps on it

    fun updateName(newName: String) {
        _userState.update { it.copy(name = newName) }
        //println("Testing StateFlow: ${_userState.value.name}")
    }
    fun clearName() = _userState.update { it.copy(name = "") }

    fun updateEmail(newEmail: String) = _userState.update { it.copy(email = newEmail) }
    fun clearEmail() = _userState.update { it.copy(email = "") }

    fun updateGender(newGender: String) = _userState.update { it.copy(gender = newGender) }
    fun clearGender() = _userState.update { it.copy(gender = "") }

    fun updateAge(newAge: String) = _userState.update { it.copy(age = newAge) }
    fun clearAge() = _userState.update { it.copy(age = "") }

    fun updateHeight(newHeight: String) = _userState.update { it.copy(height = newHeight) }
    fun clearHeight() = _userState.update { it.copy(height = "") }

    fun updateWeight(newWeight: String) = _userState.update { it.copy(weight = newWeight) }
    fun clearWeight() = _userState.update { it.copy(weight = "") }
}