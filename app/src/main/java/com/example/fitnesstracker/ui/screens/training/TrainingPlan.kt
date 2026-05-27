package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingPlanScreen(
    trainingViewModel: TrainingViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
) {
    val selectedPlan by trainingViewModel.trainingPlan.collectAsState()

    // Get the current activity from LocalContext
    val activity = LocalContext.current as Activity

    // Permissions for camera
    val permissions = listOf(Manifest.permission.CAMERA)
    val permissionsRequestCode = Random.nextInt(0, 10000)

    // Function to check if all permissions required by the app are granted
    fun hasPermissions() = permissions.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    val cameraActive = remember { mutableStateOf(false) }
    val hasPermissionsState = remember { mutableStateOf(hasPermissions()) }

    fun requestCameraPermissions() {
        if (!hasPermissions()) {
            val test = ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), permissionsRequestCode
            )
            println(":DDDDD $test")
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
            MyCameraViewFinder()
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
        } else {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
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