package com.example.fitnesstracker.ui.screens.training

import kotlinx.serialization.Serializable

@Serializable
data class CustomExercisePlans(
    val plan_id: Int = 0,
    val exercise_name: String = "",
    val exercise_id: Int = 0,
    val exercise_type: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val set_rest_time: Int = 0,
)