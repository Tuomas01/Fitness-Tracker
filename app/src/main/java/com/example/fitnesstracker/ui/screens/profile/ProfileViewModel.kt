package com.example.fitnesstracker.ui.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Handles all the data related to the user's information and the profile page
class ProfileViewModel() : ViewModel() {

    // Uses stateflow to persist and handle user information
    // UserState is a data class that has all the user information fields

    // This variable is only accessible by the viewmodel
    private val _userState = MutableStateFlow(UserState())
    // The ProfileTextFields composable uses this variable for default and updated values for the text fields
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    // Functions for updating userState, which in return updates the text fields in real time

    fun updateName(newName: String) {
        _userState.update { it.copy(name = newName) }
        //println("Testing StateFlow: ${_userState.value.name}")
    }

    fun updateEmail(newEmail: String) = _userState.update { it.copy(email = newEmail) }

    fun updateGender(newGender: String) = _userState.update { it.copy(gender = newGender) }

    fun updateAge(newAge: String) = _userState.update { it.copy(age = newAge) }

    fun updateHeight(newHeight: String) = _userState.update { it.copy(height = newHeight) }

    fun updateWeight(newWeight: String) = _userState.update { it.copy(weight = newWeight) }
}