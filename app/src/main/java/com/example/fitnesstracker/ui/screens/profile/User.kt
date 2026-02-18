package com.example.fitnesstracker.ui.screens.profile

import kotlinx.serialization.Serializable

// Data class for the user's information
@Serializable
data class User(
    val id: Int = 1,
    val name: String = "",
    val email : String = "",
    val gender : String = "",
    val age: String = "",
    val height: String = "",
    val weight: String = ""
)