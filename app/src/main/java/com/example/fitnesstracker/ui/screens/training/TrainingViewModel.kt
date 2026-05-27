package com.example.fitnesstracker.ui.screens.training

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.supabase.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    init {
        //getAllPlans()
        getTrainingPlan(2)
    }

    private val _trainingPlan = MutableStateFlow(TrainingPlan())
    val trainingPlan: StateFlow<TrainingPlan> = _trainingPlan.asStateFlow()

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    fun savePlanInfo(id: Int, name: String, type: String) {
        Log.d("TrainingVM", "savePlanInfo() id: $id, name: $name, type: $type")
        _trainingPlan.update { it.copy(id = id, name = name, type = type) }
    }

    fun allowPermissions() {
        _hasPermissions.value = true
    }

    fun getAllPlans() {
        viewModelScope.launch {
            val plans = trainingRepository.getAllTrainingPlans()
            Log.d("TrainingVM", "getAllPlans() test: $plans")
        }
    }

    fun getTrainingPlan(id: Int) {
        viewModelScope.launch {
            val plan = trainingRepository.getTrainingPlan(id)
            Log.d("TrainingVM", "getTrainingPlan() test: $plan")
        }
    }
}