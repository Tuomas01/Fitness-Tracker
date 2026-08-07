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
import com.example.fitnesstracker.ui.screens.training.mlkit.PoseDetectorProcessor
import com.example.fitnesstracker.ui.screens.training.mlkit.classification.PoseClassifierProcessor
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
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
class CameraViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    // Private variable for saving the surface request
    private val _surfaceRequests = MutableStateFlow<SurfaceRequest?>(null)

    private val appContext = context

    /*
    Public variable of the _surfaceRequests, that Composable functions can use
    to convert the surface request into a camera view
     */
    val surfaceRequests: StateFlow<SurfaceRequest?> = _surfaceRequests.asStateFlow()

    private val _inputImage = MutableStateFlow<InputImage?>(null)
    val inputImage: StateFlow<InputImage?> = _inputImage.asStateFlow()

    private val _detectedPose = MutableStateFlow<Pose?>(null)
    val detectedPose: StateFlow<Pose?> = _detectedPose.asStateFlow()

    private val _classificationResult = MutableStateFlow<List<String>>(mutableListOf())
    val classificationResult: StateFlow<List<String>> = _classificationResult.asStateFlow()

    private val TAG = "MLKitVM"

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

    val poseDetector = PoseDetectorProcessor(appContext, poseDetectorOptions)
    /**
     * Binds camera to lifecycle using lifecycleOwner, default back or front camera, image analyzer, and a built camera preview
     * @param appContext context of the app to retrieve the camera preview
     * @param lifecycleOwner the lifecycle owner of the app where the camera preview will be bound to
     */
    @OptIn(ExperimentalGetImage::class)
    suspend fun bindToCamera(lifecycleOwner: LifecycleOwner) {
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
                Log.d(TAG, "Image info: ${imageProxy.imageInfo} \n $image")

                // If the camera is showing and a frame can be accessed, processes the image using the pose detector
                // Adds an onCompleteListener to the task to get the results of the detectInImage function
                // Closes the imageProxy inside the onComplete listener as instructed in the guidelines of PoseDetection. Otherwise it would only detect a pose once and stop
                if (image != null) {
                    val result = poseDetector.detectInImage(image)
                    result.addOnCompleteListener {
                        if (result.isSuccessful) {
                            Log.d(TAG, "Task completed succesfully: \n ${result.result} \n ${result.result.pose}, ${result.result.classificationResult}")
                            if (result.result.pose.allPoseLandmarks.isNotEmpty()) {
                                _detectedPose.value = result.result.pose
                                if (result.result.classificationResult.isNotEmpty()) {
                                    _classificationResult.value = result.result.classificationResult
                                }
                            }
                        }
                        imageProxy.close()
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