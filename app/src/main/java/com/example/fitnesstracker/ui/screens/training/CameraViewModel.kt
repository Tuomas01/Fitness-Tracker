package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor
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
    val surfaceRequests: StateFlow<SurfaceRequest?> = _surfaceRequests.asStateFlow()

    private val _inputImage = MutableStateFlow<InputImage?>(null)
    val inputImage: StateFlow<InputImage?> = _inputImage.asStateFlow()

    /*
    Builds a new camera preview and uses the setSurfaceProvider function to provide a surface to the preview.
    Saves the new provided surface into the _surfaceRequest variable
     */
    private val cameraPreview = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequests.value = newSurfaceRequest
        }
    }

    /*
    ImageAnalysis that gets an image from the camera stream.
    Image will be 480x360px and only one image will be sent to be analyzed at once
     */
    private val imageAnalysis = ImageAnalysis.Builder()
        .setTargetResolution(Size(480, 360))
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // Configure the options for PoseDetector. Sets PoseDetector to stream mode
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    // Creates an instance of the PoseDetector with the configured options
    private val poseDetector = PoseDetection.getClient(options)

    // Binds camera to lifecycle using lifecycleOwner, default back or front camera and built camera preview
    @OptIn(ExperimentalGetImage::class)
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        var skippedFrames = 0
        // Start analyzing the images from camera's stream. imageProxy is a reference to the latest image
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(appContext)) { imageProxy ->

            /*
            once 60 frames have passed, so every 60 frames get the rotationDegrees from the imageInfo
            and print the imageInfo to the console
             */
            if (skippedFrames % 60 == 0) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val image: InputImage? = imageProxy.image?.let { InputImage.fromMediaImage(it, rotationDegrees) }
                _inputImage.value = image
                println("Image info: ${imageProxy.imageInfo} \n $image")
            }
            skippedFrames++

            imageProxy.close()
        }

        val cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        cameraProvider.bindToLifecycle(
            lifecycleOwner, DEFAULT_BACK_CAMERA, imageAnalysis, cameraPreview
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