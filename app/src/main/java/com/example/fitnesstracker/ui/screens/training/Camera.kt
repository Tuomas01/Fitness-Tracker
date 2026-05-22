package com.example.fitnesstracker.ui.screens.training

import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Composable function that displays camera preview on the UI
@Composable
fun MyCameraViewFinder(
    viewModel: CameraViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val currentSurfaceRequest: SurfaceRequest? by viewModel.surfaceRequests.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launch a new coroutine with the lifecycle owner being the key.
    // Uses the CameraViewModel's bindToCamera function to bind the camera to lifecycle
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    // Executes a block of code for the value in currentSurfaceRequest if it's not null.
    // This is required to convert the surfaceRequest into a camera view
    currentSurfaceRequest?.let { surfaceRequest ->

        // CoordinateTransformer for transforming from Offsets to Surface coordinates
        val coordinateTransformer = remember { MutableCoordinateTransformer() }

        // Adapter composable that displays the camera view from surfaceRequest
        // Only surfaceRequest parameter is required to get the camera to show in the application
        CameraXViewfinder(
            surfaceRequest = surfaceRequest,
            implementationMode = ImplementationMode.EXTERNAL, // Can also use EMBEDDED
            modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures {
                        with(coordinateTransformer) {
                            val surfaceCoords = it.transform()
                            viewModel.focusOnPoint(
                                surfaceRequest.resolution,
                                surfaceCoords.x,
                                surfaceCoords.y,
                            )
                        }
                    }
                },
            coordinateTransformer = coordinateTransformer,
        )
    }
}
