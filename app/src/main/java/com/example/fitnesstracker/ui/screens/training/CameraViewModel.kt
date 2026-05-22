package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    // Private variable for saving the surface request
    private val _surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    /*
    Public variable of the _surfaceRequests, that Composable functions can use
    to convert the surface request into a camera view
     */
    val surfaceRequests: StateFlow<SurfaceRequest?>
        get() = _surfaceRequests.asStateFlow()


    /*
    Builds a new camera preview and uses the setSurfaceProvider function to provide a surface to the preview.
    Saves the new provided surface into the _surfaceRequest variable
     */
    private val cameraPreview = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequests.value = newSurfaceRequest
        }
    }

    // Binds camera to lifecycle using lifecycleOwner, default back or front camera and built camera preview
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        cameraProvider.bindToLifecycle(
            lifecycleOwner, DEFAULT_BACK_CAMERA, cameraPreview
        )

        // When the lifecycle ends, unbinds all the camera useCases from the lifecycle provider and removes the from CameraX
        // The unbindAll() function ensures that the camera will be disconnected
        try {
            awaitCancellation()
        } finally {
            cameraProvider.unbindAll()
        }
    }

    fun focusOnPoint(surfaceBounds: Size, x: Float, y: Float) {

    }
}