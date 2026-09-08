package com.example.fitnesstracker.ui.screens.training

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.supabase.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import org.json.JSONObject
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

    private val _cameraActive = MutableStateFlow(false)
    val cameraActive: StateFlow<Boolean> = _cameraActive.asStateFlow()

    private val _shouldIncrement = MutableStateFlow(false)
    val shouldIncrement: StateFlow<Boolean> = _shouldIncrement.asStateFlow()

    private val _timerActive = MutableStateFlow(false)
    val timerActive: StateFlow<Boolean> = _timerActive.asStateFlow()

    private val _secondsUntilNextExercise = MutableStateFlow<Int>(0)
    val secondsUntilNextExercise: StateFlow<Int> = _secondsUntilNextExercise.asStateFlow()

    private val _currentExercise = MutableStateFlow<Int>(0)
    val currentExercise: StateFlow<Int> = _currentExercise.asStateFlow()

    private val _listOfPlans = MutableStateFlow<List<TrainingPlan>>(listOf())
    val listOfPlans: StateFlow<List<TrainingPlan>> = _listOfPlans.asStateFlow()

    private val _exercisesAndPlans =
        MutableStateFlow<JsonArray>(Json.decodeFromString<JsonArray>("[]"))
    val exercisesAndPlans: StateFlow<JsonArray> = _exercisesAndPlans.asStateFlow()

    private val _allExercisesInPlansDetails =
        MutableStateFlow<JsonArray>(Json.decodeFromString<JsonArray>("[]"))
    val allExercisesInPlansDetails: StateFlow<JsonArray> = _allExercisesInPlansDetails.asStateFlow()

    private val _sets = MutableStateFlow<Int>(0)
    private val _reps = MutableStateFlow<Int>(0)
    private val _setRestTime = MutableStateFlow<Int>(0)
    private val _exerciseList = MutableStateFlow<List<JSONObject>?>(null)
    val exerciseList: StateFlow<List<JSONObject>?> = _exerciseList.asStateFlow()

    private val TAG = "TrainingVM"

    // Call these functions when the viewModel is created
    init {
        getAllPlans()
        //getTrainingPlan(2)
        getAllExercisesInPlans()
        getAllExercisePlans()
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
                    _secondsUntilNextExercise.value = i.rest_time
                }
            }
        }
        return filteredList
    }

    // Functions for updating stateFlow variables' values

    /**
     * Saves the info of a selected plan to the _trainingPlan variable by calling the update function with the given values.
     * @param id Id of the plan
     * @param name Name of the plan
     * @param type Type of the plan
     * @param targetArea description of the target area for the plan: Full-body, Lower body or Upper body
     * @param restTime the rest time between exercises
     */
    fun savePlanInfo(id: Int, name: String, type: String, targetArea: String, restTime: Int) {
        Log.d(
            TAG,
            "savePlanInfo() id: $id, name: $name, type: $type, targetArea: $targetArea, restTime: $restTime"
        )
        _trainingPlan.update {
            it.copy(
                id = id,
                name = name,
                type = type,
                target_area = targetArea,
                rest_time = restTime
            )
        }
    }

    fun starTimer() {
        changeShouldIncrementValue(true)
        changeTimerActiveValue(true)
        viewModelScope.launch {
            while (_timerActive.value) {
                countdownSecondsUntilNextExercise()
                if (_secondsUntilNextExercise.value == 0) {
                    incrementCurrentExercise()
                    addSecondsUntilNextExercise(_trainingPlan.value.rest_time)
                    changeShouldIncrementValue(false)
                    changeTimerActiveValue(false)
                }
                delay(1000)
            }
        }
    }

    fun stopTimer() {
        changeShouldIncrementValue(false)
        changeTimerActiveValue(false)
    }

    fun skipExercise() {
        stopTimer()
        incrementCurrentExercise()
        addSecondsUntilNextExercise(_trainingPlan.value.rest_time)
    }

    fun allowPermissions() {
        _hasPermissions.value = true
    }

    fun changeCameraActiveValue() {
        _cameraActive.value = !_cameraActive.value
    }

    fun countdownSecondsUntilNextExercise() {
        _secondsUntilNextExercise.value--
    }

    fun addSecondsUntilNextExercise(seconds: Int) {
        _secondsUntilNextExercise.value = seconds
    }

    fun incrementCurrentExercise() {
        _currentExercise.value++
    }

    fun resetCurrentExercise() {
        stopTimer()
        _currentExercise.value = 0
        addSecondsUntilNextExercise(_trainingPlan.value.rest_time)
    }

    fun changeShouldIncrementValue(newShouldIncrementValue: Boolean) {
        _shouldIncrement.value = newShouldIncrementValue
    }

    fun changeTimerActiveValue(newTimerActiveValue: Boolean) {
        _timerActive.value = newTimerActiveValue
    }

    // Functions related to database operations utilizing TrainingRepository.kt

    /**
     * Uses the trainingRepository function getAllTrainingPlans() to retrieve all training plans from the database and saves it to a variable.
     */
    fun getAllPlans() {
        viewModelScope.launch {
            val plans = trainingRepository.getAllTrainingPlans()
            Log.d(TAG, "getAllPlans() test: $plans")
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
            Log.d(TAG, "getTrainingPlan() test: $plan")
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
                Log.d(TAG, "getAllExercisesInPlans() test: $exercisesInPlans")
                if (exercisesInPlans !== null) {
                    // Convert the JSON String into a JSON array using the Json.decodeFromString<>() method
                    val jsonObject = Json.decodeFromString<JsonArray>(exercisesInPlans)
                    _exercisesAndPlans.value = jsonObject
                    //Log.d(TAG, "getAllExercisesInPlans() _exercisesAndPlans value: ${_exercisesAndPlans.value}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "getAllExercisesInPlans() error: $e")
            }
        }
    }

    fun getAllExercisePlans() {
        viewModelScope.launch {
            try {
                val exercisePlans = trainingRepository.fetchAllExercisePlans()
                Log.d(TAG, "getAllExercisePlans test: $exercisePlans")
                if (exercisePlans !== null) {
                    val jsonObject = Json.decodeFromString<JsonArray>(exercisePlans)
                    _allExercisesInPlansDetails.value = jsonObject
                    Log.d(
                        TAG,
                        "getAllExercisePlans testing json ${_allExercisesInPlansDetails.value}"
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "getAllExercisePlans() error: $e")
            }
        }
    }

    /**
     * Loops through the _exercisesAndPlans variable if the value is not empty.
     * The variable's value is either an empty JSON array by default or a JSON array from getAllExercisesInPlans() function.
     *
     * Has 2 nested loop that go through the training plans in which the exercise was found in, and if the training plan id is the same as the parameter planId, the function
     * saves all the exercise details into a list as Json.
     */
    fun addExercisesToPlan(planId: Int) {
        viewModelScope.launch {
            try {
                if (!_exercisesAndPlans.value.isEmpty()) {
                    val exerciseList = mutableListOf<JSONObject>()
                    val customExercisePlansSet = mutableMapOf<String, String>()
                    // Loop through the exercises and the training plans where the exercise is linked to the plans
                    for (i in _exercisesAndPlans.value) {
                        Log.d(TAG, "addExercisesToPlan() json: $i")
                        // Save the training_plans JSON array into a variable
                        // Example of what i.jsonObject["training_plans"] returns: [{"plan_id":3, "plan_name":"Lower body"}]
                        val exerciseName = i.jsonObject["name"]
                        val exerciseId = i.jsonObject["id"]
                        val exerciseType = i.jsonObject["exercise_type"]
                        val trainingPlans = i.jsonObject["training_plans"]
                        Log.d(
                            TAG,
                            "addExercisesToPlan() json variables: $exerciseName, $exerciseId, $exerciseType, $trainingPlans"
                        )
                        //Log.d(TAG, "addExercisesToPlan() name and plans: ${i.jsonObject["name"]}, ${trainingPlans?.jsonArray}}")
                        /*
                         If the JSON response included data in the training_plans array,
                         loop through training_plans and if the plan_id is equal to the parameter planId, save the exercises and their details into a list.
                         */
                        if (!trainingPlans?.jsonArray.isNullOrEmpty()) {
                            for (y in trainingPlans.jsonArray) {
                                val yPlanId = y.jsonObject["plan_id"]
                                if (yPlanId.toString().toInt() == planId &&
                                    _allExercisesInPlansDetails.value.isNotEmpty()
                                ) {
                                    Log.d(
                                        TAG, "addExercisesToPlan() nested condition: " +
                                                "$y, $yPlanId, $exerciseName $exerciseId"
                                    )
                                    // Loop through the _allExercisesInPlansDetails value, which is a JsonArray.
                                    // Get the sets, reps and set rest time values where the plan id and exercise id matches the selected plan's ids
                                    for (z in _allExercisesInPlansDetails.value) {
                                        val zExerciseName = i.jsonObject["name"]
                                        if (z.jsonObject["plan_id"] == yPlanId && z.jsonObject["exercise_id"] == exerciseId) {
                                            _sets.value =
                                                z.jsonObject["sets"].toString().toInt()
                                            _reps.value =
                                                z.jsonObject["reps"].toString().toInt()
                                            _setRestTime.value =
                                                z.jsonObject["set_rest_time"].toString().toInt()

                                            // Removes quotes from the start and end of the string
                                            val trimmedExerciseType = exerciseType.toString().drop(1).dropLast(1)
                                            val trimmedExerciseName = exerciseName.toString().drop(1).dropLast(1)
                                            Log.d(
                                                TAG,
                                                "addExercisesToPlan() exercise details: name: $trimmedExerciseName, id: $exerciseId, type: " +
                                                        "$trimmedExerciseType, sets: ${_sets.value}, reps: ${_reps.value}, rest time between sets: ${_setRestTime.value}"
                                            )
                                            customExercisePlansSet["plan_id"] = yPlanId.toString()
                                            customExercisePlansSet["exercise_name"] = trimmedExerciseName
                                            customExercisePlansSet["exercise_id"] = exerciseId.toString()
                                            customExercisePlansSet["exercise_type"] = trimmedExerciseType
                                            customExercisePlansSet["sets"] = _sets.value.toString()
                                            customExercisePlansSet["reps"] = _reps.value.toString()
                                            customExercisePlansSet["set_rest_time"] = _setRestTime.value.toString()
                                            val exerciseDetailsAsJson = JSONObject(customExercisePlansSet)
                                            exerciseList.add(exerciseDetailsAsJson)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _exerciseList.value = exerciseList
                    Log.d(TAG, "addExercisesToPlan() exercise details as json: ${_exerciseList.value}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "addExercisesToPlan() error: $e")
            }
        }
    }
}