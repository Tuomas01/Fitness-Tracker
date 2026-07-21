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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

/**
 * TrainingViewModel that is a HiltViewModel so that the trainingRepository can be injected into the constructor.
 * Handles all the logic related to the training plans and exercises in those plans.
 * Uses the database functions from the repository to get the data and use the data.
 *
 * Uses private values to store data and expose it to the UI by getting the private values as StateFlow
 */
@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _trainingPlan = MutableStateFlow(TrainingPlan())
    val trainingPlan: StateFlow<TrainingPlan> = _trainingPlan.asStateFlow()

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _listOfPlans = MutableStateFlow<List<TrainingPlan>>(listOf())
    val listOfPlans: StateFlow<List<TrainingPlan>> = _listOfPlans.asStateFlow()

    private val _exercisesAndPlans =
        MutableStateFlow<JsonArray>(Json.decodeFromString<JsonArray>("[]"))
    val exercisesAndPlans: StateFlow<JsonArray> = _exercisesAndPlans.asStateFlow()

    private val _listOfExercises = MutableStateFlow<List<String>>(listOf())
    val listOfExercises: StateFlow<List<String>> = _listOfExercises.asStateFlow()

    init {
        getAllPlans()
        //getTrainingPlan(2)
        getAllExercisesInPlans()
    }

    /**
     * Saves the info of a selected plan to the _trainingPlan variable by calling the update function with the given values.
     * @param id Id of the plan
     * @param name Name of the plan
     * @param type Type of the plan
     */
    fun savePlanInfo(id: Int, name: String, type: String) {
        Log.d("TrainingVM", "savePlanInfo() id: $id, name: $name, type: $type")
        _trainingPlan.update { it.copy(id = id, name = name, type = type) }
    }

    fun allowPermissions() {
        _hasPermissions.value = true
    }

    /**
     * Uses the trainingRepository function getAllTrainingPlans() to retrieve all training plans from the database and saves it to a variable.
     */
    fun getAllPlans() {
        viewModelScope.launch {
            val plans = trainingRepository.getAllTrainingPlans()
            Log.d("TrainingVM", "getAllPlans() test: $plans")
            if (!plans.isNullOrEmpty()) {
                _listOfPlans.value = plans
            }
        }
    }

    /**
     * Gets a training plan with the given ID
     * @param id ID of the plan
     */
    fun getTrainingPlan(id: Int) {
        viewModelScope.launch {
            val plan = trainingRepository.getTrainingPlan(id)
            Log.d("TrainingVM", "getTrainingPlan() test: $plan")
        }
    }

    /**
     * Calls the trainingRepository's fetchAllExercisesPlans() function to get a response of JSON as a string
     * that includes exercise name, id, training plan id and training plan name.
     *
     * If the response isn't null, converts the JSON String into a JSON array and adds it to the _exercisesAndPlans variable.
     * Wrapped in try catch to print an error to the console if something goes wrong.
     */
    fun getAllExercisesInPlans() {
        viewModelScope.launch {
            try {
                val exercisesInPlans = trainingRepository.fetchAllExercisesInPlans()
                Log.d("TrainingVM", "getAllExercisesInPlans() test: $exercisesInPlans")
                if (exercisesInPlans !== null) {
                    // Convert the JSON String into a JSON array using the Json.decodeFromString<>() method
                    val jsonObject = Json.decodeFromString<JsonArray>(exercisesInPlans)
                    _exercisesAndPlans.value = jsonObject
                    //Log.d("TrainingVM", "getAllExercisesInPlans() _exercisesAndPlans value: ${_exercisesAndPlans.value}")
                }
            } catch (e: Exception) {
                Log.d("TrainingVM", "getAllExercisesInPlans() error: $e")
            }
        }
    }

    /**
     * Loops through the _exercisesAndPlans variable if the value is not empty.
     * The variable's value is either an empty JSON array by default or a JSON array from getAllExercisesInPlans() function.
     *
     * Has a nested loop to go through the training plans in which the exercise was found in and if the training plan id is the same as the parameter planId,
     * saves all the exercise names into a list.
     */
    fun addExercisesToPlan(planId: Int) {
        viewModelScope.launch {
            try {
                _listOfExercises.value = mutableListOf<String>()
                if (!_exercisesAndPlans.value.isEmpty()) {
                    val exerciseList = mutableListOf<String>()
                    // Loop through the exercises and the training plans where the exercise is linked to the plans
                    for (i in _exercisesAndPlans.value) {
                        Log.d("TrainingVM", "addExercisesToPlan() json: $i")
                        // Save the training_plans JSON array into a variable
                        // Example of what i.jsonObject["training_plans"] returns: [{"plan_id":3, "plan_name":"Lower body"}]
                        val trainingPlans = i.jsonObject["training_plans"]
                        //Log.d("TrainingVM", "addExercisesToPlan() name and plans: ${i.jsonObject["name"]}, ${trainingPlans?.jsonArray}}")
                        /*
                         If the JSON response included data in the training_plans array,
                         loop through training_plans and if the plan_id is equal to the parameter PlanId, save the name of the exercise to a list.
                         */
                        if (!trainingPlans?.jsonArray.isNullOrEmpty()) {
                            for (y in trainingPlans.jsonArray) {
                                if (y.jsonObject["plan_id"].toString().toInt() == planId) {
                                    val exerciseName = i.jsonObject["name"]
                                    val exerciseId = i.jsonObject["id"]
                                    val yplanId = y.jsonObject["plan_id"]
                                    Log.d(
                                        "TrainingVM", "addExercisesToPlan() nested condition: " +
                                                "$yplanId, $exerciseName $exerciseId"
                                    )
                                    exerciseList.add(exerciseName.toString())
                                }
                            }
                        }
                    }
                    _listOfExercises.value = exerciseList
                }
            } catch (e: Exception) {
                Log.d("TrainingVM", "addExercisesToPlan() error: $e")
            }
        }
    }

    /**
     * Loops through all the plans and saves the plans that match the value with the given parameter into a mutable list of TrainingPlans.
     * @param type String that refers to the type of the plan.
     * @return A list of TrainingPlans
     */
    fun filterTrainingPlans(type: String): List<TrainingPlan> {
        val filteredList = mutableListOf<TrainingPlan>()
        viewModelScope.launch {
            for (i in _listOfPlans.value) {
                if (i.type == type || i.type.contains(type)) {
                    filteredList.add(i)
                }
            }
        }
        return filteredList
    }
}