package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.MainActivity
import kotlin.random.Random

@Composable
fun TrainingScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    // Get the current activity from LocalContext
    val activity = LocalContext.current as Activity

    // Permissions for camera
    val permissions = listOf(Manifest.permission.CAMERA)
    val permissionsRequestCode = Random.nextInt(0, 10000)

    // Function to check if all permissions required by the app are granted
    fun hasPermissions() = permissions.all {
        ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                activity, permissions.toTypedArray(), permissionsRequestCode
            )
        } else {
            Toast.makeText(activity,
                "Permissions have been granted",
                Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { requestCameraPermissions() },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Recording") },
                text = { Text("Start camera") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        MyCameraViewFinder()
    }
}
