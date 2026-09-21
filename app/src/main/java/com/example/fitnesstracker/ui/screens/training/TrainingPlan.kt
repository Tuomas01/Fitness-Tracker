package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.SwapHorizontalCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.random.Random
import com.example.fitnesstracker.ui.screens.training.mlkit.PoseOverlay
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.security.Permission

/**
 * Training plan screen composable. The screen that shows after clicking on an arrow to go to a training plan on the training page.
 * @param trainingViewModel A shared viewModel that is created inside the NavHost
 * @param navigateBack A function to navigate back to the previous screen
 * @param cameraViewModel CameraViewModel that is a hilt viewModel created inside the constructor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    trainingViewModel: TrainingViewModel,
    navigateBack: () -> Unit,
    cameraViewModel: CameraViewModel = hiltViewModel(),
) {
    val selectedPlan by trainingViewModel.trainingPlan.collectAsState()
    val exerciseDetails by trainingViewModel.exerciseList.collectAsState()

    // Boolean values from training viewmodel. The values are changed with change value functions in the view model
    val cameraActive by trainingViewModel.cameraActive.collectAsState()
    val hasPermissionsState by trainingViewModel.hasPermissions.collectAsState()
    val timerActive by trainingViewModel.timerActive.collectAsState()
    val enabledButton by trainingViewModel.buttonEnabled.collectAsState()

    val secondsUntilNextExercise by trainingViewModel.secondsUntilNextExercise.collectAsState()
    val currentExerciseNumber by trainingViewModel.currentExercise.collectAsState()

    val inputImage by cameraViewModel.inputImage.collectAsStateWithLifecycle()
    val poseDetectorActive by cameraViewModel.poseDetectorActive.collectAsStateWithLifecycle()

    // Get the current activity from LocalContext
    val activity = LocalContext.current as Activity

    val poseResult by cameraViewModel.detectedPose.collectAsState()
    val poseDetails by cameraViewModel.classificationResult.collectAsState()

    // Permissions for camera
    val permissions = listOf(Manifest.permission.CAMERA)
    val permissionsRequestCode = Random.nextInt(0, 10000)

    // Function to check if all permissions required by the app are granted
    fun hasPermissions() = permissions.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    val screenWidth = remember { mutableFloatStateOf(1f) }
    val screenHeight = remember { mutableFloatStateOf(1f) }

    val currentExercise = exerciseDetails?.get(currentExerciseNumber)
    val currentExerciseName = currentExercise?.get("exercise_name")
    val currentExerciseSetRestTime = currentExercise?.get("set_rest_time")
    val currentExerciseReps = currentExercise?.get("reps")

    // This was causing the app to crash when navigating away from the training plan
    /*val exercise = Json.decodeFromString<CustomExercisePlans>(
        exerciseDetails?.get(currentExercise).toString()
    )*/

    fun requestCameraPermissions() {
        if (!hasPermissions()) {
            val test = ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), permissionsRequestCode
            )
        } else {
            trainingViewModel.allowPermissions()
            trainingViewModel.changeCameraActiveValue()
        }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = selectedPlan.name.ifBlank {
                            "Training plan"
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Arrow icon indicating navigation back to previous screen"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (hasPermissionsState && cameraActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { screen ->
                        screenWidth.floatValue = screen.size.width.toFloat()
                        screenHeight.floatValue = screen.size.height.toFloat()
                    }
            ) {
                MyCameraViewFinder()
                if (poseResult != null && inputImage != null && poseDetectorActive) {
                    PoseOverlay(
                        poseResult!!.allPoseLandmarks,
                        inputImage!!.width,
                        inputImage!!.height,
                        screenWidth.floatValue + 450,
                        screenHeight.floatValue - 475
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!poseDetails.isNullOrEmpty() && poseDetectorActive) {
                        val substringExerciseName = currentExerciseName.toString().substring(0, 4).lowercase()
                        Text(
                            modifier = Modifier.padding(16.dp, 8.dp),
                            textAlign = TextAlign.Center,
                            text =
                                if (!poseDetails[1].isNullOrEmpty() && poseDetails[2].length > 3 && poseDetails[0].substring(0, 4).lowercase().contains(substringExerciseName)) {
                                    if (poseDetails[1].toInt() == currentExerciseReps.toString().toInt()) {
                                        if (currentExerciseNumber + 1 < exerciseDetails!!.size) {
                                            trainingViewModel.starTimer()
                                            cameraViewModel.closePoseDetector()
                                            cameraViewModel.resetCounter()
                                        } else {
                                            cameraViewModel.closePoseDetector()
                                            trainingViewModel.resetCurrentExercise()
                                            cameraViewModel.resetCounter()
                                        }
                                    }
                                    "\nDetected pose: ${poseDetails[0]}\n${poseDetails[2]}\n${poseDetails[3]}"
                                } else {
                                    "\nPose not detected or the detected pose does not match the current exercise"
                                }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceBright)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        if (exerciseDetails != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Current exercise: ${currentExerciseName}\n" +
                                            if (currentExerciseNumber < exerciseDetails!!.size - 1) {
                                                trainingViewModel.enableButton()
                                                "Next exercise in ${secondsUntilNextExercise}s: ${
                                                    exerciseDetails?.get(
                                                        if (currentExerciseNumber + 1 < exerciseDetails!!.size) {
                                                            currentExerciseNumber + 1
                                                        } else {
                                                            0
                                                        }
                                                    )["exercise_name"]
                                                }"
                                            } else {
                                                trainingViewModel.disableButton()
                                                ""
                                            }
                                )
                                if (currentExerciseName == "Push-ups" || currentExerciseName == "Squats") {
                                    cameraViewModel.activatePoseDetector()
                                } else {
                                    cameraViewModel.closePoseDetector()
                                }
                                Column(
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            cameraViewModel.changeCamera()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Cameraswitch,
                                            contentDescription = "Switch camera icon indicating swapping between back and front camera"
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    trainingViewModel.resetCurrentExercise()
                                }
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Restart icon indicating restarting the plan"
                                )
                            }
                            IconButton(
                                onClick = {
                                    trainingViewModel.previousExercise()
                                },
                                enabled = currentExerciseNumber != 0
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Icon indicating going back to the previous exercise"
                                )
                            }
                            if (timerActive) {
                                IconButton(
                                    onClick = {
                                        trainingViewModel.stopTimer()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.PauseCircle,
                                        contentDescription = "Start icon indicating pausing the timer"
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        trainingViewModel.starTimer()
                                        println("Variable test: 1: $currentExerciseNumber, 2: $currentExerciseSetRestTime, 3: $currentExerciseName, 4: $currentExercise, 5: $currentExerciseReps, " +
                                                "6: ${exerciseDetails!!.size}, 7: $exerciseDetails")
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = "Start icon indicating starting the timer"
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    trainingViewModel.skipExercise()
                                },
                                enabled = enabledButton
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Icon indicating going to the next exercise"
                                )
                            }
                            IconButton(
                                onClick = {
                                    trainingViewModel.changeCameraActiveValue()
                                    trainingViewModel.resetCurrentExercise()
                                }
                            ) {
                                Icon(
                                    Icons.Default.CloseFullscreen,
                                    contentDescription = "Close fullscreen icon indicating closing the camera view"
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    //.border(2.dp, Color.Red)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!exerciseDetails.isNullOrEmpty()) {
                    ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                        ) {
                            for (i in exerciseDetails) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Use the substring function to remove quotes from the string
                                    Text(
                                        i["exercise_name"].toString(),
                                        fontSize = 24.sp
                                    )
                                    HorizontalDivider(thickness = 2.dp)
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    modifier = Modifier
                                        .align(Alignment.Start)
                                        .padding(top = 20.dp),
                                    text = "Rest time between exercises: ${selectedPlan.rest_time}S"
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    requestCameraPermissions()
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                Row() {
                                    Text("Start plan")
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera icon",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}