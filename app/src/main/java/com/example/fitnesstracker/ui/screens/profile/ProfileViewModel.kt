package com.example.fitnesstracker.ui.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

// Handles all the data related to the user's information and the profile page
class ProfileViewModel() : ViewModel() {

    // User information taken from text fields
    var name by mutableStateOf(TextFieldValue(""))
    var email by mutableStateOf(TextFieldValue(""))
    var gender by mutableStateOf(TextFieldValue(""))
    var age by mutableStateOf(TextFieldValue(""))
    var height by mutableStateOf(TextFieldValue(""))
    var weight by mutableStateOf(TextFieldValue(""))

    // Functions for updating the user information locally
    fun updateName(newName: TextFieldValue) {
        name = newName
        //println("Testing name: $name and $newName")
    }

    fun updateEmail(newEmail: TextFieldValue) {
        email = newEmail
    }

    fun updateGender(newGender: TextFieldValue) {
        gender = newGender
    }

    fun updateAge(newAge: TextFieldValue) {
        age = newAge
    }

    fun updateHeight(newHeight: TextFieldValue) {
        height = newHeight
    }

    fun updateWeight(newWeight: TextFieldValue) {
        weight = newWeight
    }

    /* A different way of handling the text field data, but the data doesn't persist the same way

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState>
        get() = _userState.asStateFlow()

    fun updateName(newName: String) {
        _userState.update { it.copy(name = newName) }
        println("Testing StateFlow: $userState")
    }

    fun updateEmail(newEmail: String) = _userState.update { it.copy(email = newEmail) }

    fun updateGender(newGender: String) = _userState.update { it.copy(gender = newGender) }

    fun updateAge(newAge: String) = _userState.update { it.copy(age = newAge) }

    fun updateHeight(newHeight: String) = _userState.update { it.copy(height = newHeight) }

    fun updateWeight(newWeight: String) = _userState.update { it.copy(weight = newWeight) }
    */
}