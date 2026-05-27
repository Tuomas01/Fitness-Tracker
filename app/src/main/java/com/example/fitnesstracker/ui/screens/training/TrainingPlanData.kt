package com.example.fitnesstracker.ui.screens.training

import kotlinx.serialization.Serializable

@Serializable
data class TrainingPlan(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val target_area: String = "",
)