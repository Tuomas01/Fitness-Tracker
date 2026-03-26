package com.example.fitnesstracker.ui.screens.profile

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
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

    // Another userState variable that holds user's data from the database.
    // These values won't get overridden by the text fields being empty.
    // This data will be used instead if the user tries to update their information by leaving the fields empty
    private val _userStateFromDb = MutableStateFlow(User())
    val userStateFromDb: StateFlow<User> = _userStateFromDb.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    private val _genderOption = MutableStateFlow("")
    val genderOption: StateFlow<String> = _genderOption.asStateFlow()

    init {
        getIdFromSession()
        getUser(_userId.value)
    }

    fun getUsers() {
        viewModelScope.launch {
            userRepository.getUsers()
        }
    }

    fun getUser(id: String) {
        viewModelScope.launch {
            val user = userRepository.getUser(id)
            Log.d("ProfileVM", "Testing getUser: $user")
            _userState.emit(user)
            Log.d("ProfileVM", "userState: ${_userState.value}")
            _userStateFromDb.emit(user)
            if (_userId.value != user.id) {
                Log.d("ProfileVM", "getUser() anonymous login")
                _isAnonymous.value = true
            } else {
                _isAnonymous.value = false
                Log.d("ProfileVM", "getUser() not anonymous")
            }
        }
    }

    fun getIdFromSession() {
        viewModelScope.launch {
            val user = authRepository.retrieveUserFromSession()
            Log.d("ProfileVM", "getIdFromSession() test: $user")
            if (user !== null) {
                _userId.value = user.id
                Log.d("ProfileVM", "userId test: ${user.id} | ${_userId.value}")
            } else {
                Log.d("ProfileVM", "getIdFromSession(): no active session found")
            }
        }
    }

    fun updateUser(): Boolean {
        // Check if the text fields are empty and if they are add user's information from database to the _userState object
        // This should be improved to make it cleaner.
        // Instead of using multiple if checks there has to be a way to iterate through all the _userState values
        val success = mutableStateOf(false)
        if (_userState.value.name.isEmpty()) {
            _userState.update { it.copy(name = _userStateFromDb.value.name) }
        }

        if (_userState.value.age.isEmpty()) {
            _userState.update { it.copy(age = _userStateFromDb.value.age) }
        }

        if (_userState.value.height.isEmpty()) {
            _userState.update { it.copy(height = _userStateFromDb.value.height) }
        }

        if (_userState.value.weight.isEmpty()) {
            _userState.update { it.copy(weight = _userStateFromDb.value.weight) }
        }

        // Calls the userRepository's updateUser() function to update the user information in the database
        try {
            viewModelScope.launch {
                try {
                    val response = userRepository.updateUser(
                        id = _userId.value,
                        name = _userState.value.name,
                        gender = _userState.value.gender,
                        age = _userState.value.age,
                        height = _userState.value.height,
                        weight = _userState.value.weight
                    )
                    Log.d("ProfileVM", "updateUser() test: $response")
                } catch (e: Exception) {
                    Log.d("ProfileVM", "updateUser() error: $e")
                }
            }
            if (_userState.value.email.isNotEmpty() && _userState.value.email !== _userStateFromDb.value.email) {
                updateUserEmail()
            }
            success.value = true
        } catch (e: Exception) {
            Log.d("ProfileVM", "updateUser() outer error: $e")
            success.value = false
        }
        return success.value
    }

    fun updateUserEmail(): Boolean {
        if (_userState.value.email.isNotEmpty()) {
            try {
                viewModelScope.launch {
                    val success = authRepository.updateUserEmail(_userState.value.email)
                    Log.d("ProfileVM", "updateUserEmail() test: $success")
                }
                return true
            } catch (e: Exception) {
                Log.d("ProfileVM", "updateUserEmail() error: $e")
                return false
            }
        } else {
            Log.d("ProfileVM", "updateUserEmail() error: email field was not filled")
            return false
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

    fun updateGender() = _userState.update { it.copy(gender = _genderOption.value) }

    fun updateGenderOption(newGender: String) {
        if (newGender === "Leave empty") {
            _genderOption.value = ""
        } else {
            _genderOption.value = newGender
        }
    }

    fun updateAge(newAge: String) = _userState.update { it.copy(age = newAge) }
    fun clearAge() = _userState.update { it.copy(age = "") }

    fun updateHeight(newHeight: String) = _userState.update { it.copy(height = newHeight) }
    fun clearHeight() = _userState.update { it.copy(height = "") }

    fun updateWeight(newWeight: String) = _userState.update { it.copy(weight = newWeight) }
    fun clearWeight() = _userState.update { it.copy(weight = "") }
}