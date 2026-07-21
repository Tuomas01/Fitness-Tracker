package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.SurfaceRequest
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.random.Random
import com.example.fitnesstracker.ui.screens.training.mlkit.PoseOverlay

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
    val exercises by trainingViewModel.listOfExercises.collectAsState()
    val inputImage by cameraViewModel.inputImage.collectAsStateWithLifecycle()

    // Get the current activity from LocalContext
    val activity = LocalContext.current as Activity

    val poseResult by cameraViewModel.detectedPose.collectAsState()

    // Permissions for camera
    val permissions = listOf(Manifest.permission.CAMERA)
    val permissionsRequestCode = Random.nextInt(0, 10000)

    // Function to check if all permissions required by the app are granted
    fun hasPermissions() = permissions.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    val cameraActive = remember { mutableStateOf(false) }
    val hasPermissionsState = remember { mutableStateOf(hasPermissions()) }

    val screenWidth = remember { mutableFloatStateOf(1f) }
    val screenHeight = remember { mutableFloatStateOf(1f) }

    fun requestCameraPermissions() {
        if (!hasPermissions()) {
            val test = ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), permissionsRequestCode
            )
        } else {
            hasPermissionsState.value = true
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
        if (hasPermissionsState.value && cameraActive.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { screen ->
                        screenWidth.floatValue = screen.size.width.toFloat()
                        screenHeight.floatValue = screen.size.height.toFloat()
                    }
            ) {
                MyCameraViewFinder()
                if (poseResult != null && inputImage != null) {
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
                    Button(
                        onClick = {
                            cameraActive.value = false
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Disable camera")
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
                if (!exercises.isEmpty()) {
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
                            for (i in exercises) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Use the substring function to remove quotes from the string
                                    Text(
                                        i.substring(1, i.length - 1),
                                        fontSize = 24.sp
                                    )
                                    HorizontalDivider(thickness = 2.dp)
                                }
                            }
                        }
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
                            cameraActive.value = true
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