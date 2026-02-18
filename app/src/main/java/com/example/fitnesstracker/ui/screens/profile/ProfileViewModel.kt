package com.example.fitnesstracker.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.supabase.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Handles all the data related to the user's information and the profile page
class ProfileViewModel() : ViewModel() {

    // Uses stateflow to persist and handle user information
    // UserState is a data class that has all the user information fields

    // This variable is only accessible by the viewmodel
    private val _userState = MutableStateFlow(User())

    // The ProfileTextFields composable uses this variable for default and updated values for the text fields
    val userState: StateFlow<User> = _userState.asStateFlow()

    init {
        getUsers()
    }

    fun getUsers() {
        viewModelScope.launch {
            val user = supabase.from("User")
                .select().decodeList<User>()
            println("Testing supabase: $user \n name: ${user[0].name}")
            _userState.emit(user[0])
        }
    }

    /*fun getUser() {
        viewModelScope.launch {
            val user = userRepository.getUser("1")
            _userState.emit(user)
        }
    }*/

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

    // Adds conditional checks to text field values, so that the app doesn't crash if the value is empty
    // An example of this is if the user deletes a value, the app would crash but with the conditional check, the app doesn't crash
    /*fun updateAge(newAge: String) {
        if (!newAge.isEmpty()) {
            println("newAge: $newAge")
            _userState.update {
                it.copy(age = newAge.toInt())
            }
        } else {
            println("newAge is empty: $newAge")
        }
    }

    fun updateHeight(newHeight: String) {
        if (!newHeight.isEmpty()) {
            println("newHeight: $newHeight")
            _userState.update { it.copy(height = newHeight.toInt()) }
        } else {
            println("newHeight is empty: $newHeight")
        }
    }

    fun updateWeight(newWeight: String) {
        if (!newWeight.isEmpty()) {
            println("newWeight: $newWeight")
            _userState.update { it.copy(weight = newWeight.toInt()) }
        } else {
            println("newWeight is empty: $newWeight")
        }
    }*/
}