package com.example.fitnesstracker.ui.screens.training

import kotlinx.serialization.Serializable

/**
 * Data class that represents the data stored in the database related to the training plans
 */
@Serializable
data class TrainingPlan(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val target_area: String = "",
)