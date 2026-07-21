package com.example.fitnesstracker.ui.screens.training

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.random.Random

/**
 * CameraViewModel that is a HiltViewModel. Handles all the logic for binding the camera to the lifecycle.
 * The pose detector creation and the image analysis is handled here.
 *
 * Uses private values to store data and expose it to the UI by getting the private values as StateFlow
 */
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

    private val _detectedPose = MutableStateFlow<Pose?>(null)
    val detectedPose: StateFlow<Pose?> = _detectedPose.asStateFlow()

    /**
     * Builds a new camera preview and uses the setSurfaceProvider function to provide a surface to the preview.
     *
     * Saves the new provided surface into the _surfaceRequest variable
     */
    private val cameraPreview = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequests.value = newSurfaceRequest
        }
    }

    /**
     * ImageAnalysis that gets an image from the camera stream.
     *
     * Image will be 480x360px and only one image will be sent to be analyzed at once
     */
    private val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // Configure the options for PoseDetector. Sets PoseDetector to stream mode
    private val poseDetectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    // Creates an instance of the PoseDetector with the configured options
    private val poseDetector = PoseDetection.getClient(poseDetectorOptions)

    /**
     * Binds camera to lifecycle using lifecycleOwner, default back or front camera, image analyzer, and a built camera preview
     * @param appContext context of the app to retrieve the camera preview
     * @param lifecycleOwner the lifecycle owner of the app where the camera preview will be bound to
     */
    @OptIn(ExperimentalGetImage::class)
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        // Start analyzing the images from camera's stream. imageProxy is a reference to the latest image
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(appContext)) { imageProxy ->

            /*
            Launches a viewModel coroutine in which a new InputImage is created from the imageProxy
            and the image is sent to the Pose Detector for processing.
            Delays the start of execution by 1 seconds so that a frame is only processed ever 1 seconds
            instead of every frame.
             */
            viewModelScope.launch {
                //delay(1000)
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val image: InputImage? =
                    imageProxy.image?.let { InputImage.fromMediaImage(it, rotationDegrees) }
                _inputImage.value = image
                Log.d("MLKitVM", "Image info: ${imageProxy.imageInfo} \n $image")

                // If the camera is showing and a frame can be accessed, processes the image using the pose detector
                // Event listeners are added to the pose detector and the result accessed through the event listeners lambdas.
                // Closes the imageProxy inside the onComplete listener as instructed in the guidelines of PoseDetection
                if (image != null) {
                    poseDetector.process(image)
                        .addOnSuccessListener { results ->
                            Log.d(
                                "MLKitVM",
                                "detectPose() onSuccessListener: $results || ${results.allPoseLandmarks}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.d(
                                "MLKitVM",
                                "detectPose() onFailureListener: $e"
                            )
                        }
                        .addOnCanceledListener {
                            Log.d(
                                "MLKitVM",
                                "detectPose() onCanceledListener"
                            )
                        }
                        .addOnCompleteListener { result ->
                            Log.d(
                                "MLKitVM",
                                "detectPose() onCompleteListener: ${result.result} || ${result.result.allPoseLandmarks}"
                            )
                            imageProxy.close()
                        }
                        .continueWith(
                            ContextCompat.getMainExecutor(appContext)
                        ) { task ->
                            Log.d("MLKitVM", "task: $task")
                            val pose = task.getResult()
                            Log.d("MLKitVM", "pose: $pose || ${pose.allPoseLandmarks}")
                            _detectedPose.value = pose
                        }
                }
            }
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
}