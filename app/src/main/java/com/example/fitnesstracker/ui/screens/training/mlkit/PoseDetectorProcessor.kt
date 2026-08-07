package com.example.fitnesstracker.ui.screens.training.mlkit

import android.content.Context
import android.util.Log
import com.example.fitnesstracker.ui.screens.training.mlkit.classification.PoseClassifierProcessor
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import java.util.concurrent.Executors

/**
 * A custom class for PoseDetector
 * The class holds the logic for detecting a pose from the camera preview. Uses PoseClassifierProcessor to get the results of the pose
 * @param context appContext
 * @param options the options that the PoseDetector will be built with
 */
class PoseDetectorProcessor(
    private val context: Context,
    options: PoseDetectorOptionsBase,
) {
    private val detector = PoseDetection.getClient(options)
    private val classificationExecutor = Executors.newSingleThreadExecutor()

    private var poseClassifierProcessor: PoseClassifierProcessor? = null

    private val TAG = "PoseDetectorProcessor"

    /**
     * Custom class that holds the Pose and the details of the pose in the classificationResult
     */
    class PoseWithClassification(val pose: Pose, val classificationResult: List<String>)

    fun stop() {
        detector.close()
    }

    /**
     * Function for detecting a pose from the given image.
     *
     * Creates a new PoseClassifierProcessor to get details of the pose; Rep count, name of the pose and confidence of the pose
     * @param image Input image
     * @return A Task that is of the type PoseWithClassification. PoseWithClassification is a custom class that holds the pose and the details of the pose
     */
    fun detectInImage(image: InputImage): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor,
                { task ->
                    val pose = task.getResult()
                    var classificationResult: List<String> = ArrayList()
                    Log.d(TAG, "Image processing started")
                    if (poseClassifierProcessor == null) {
                        Log.d(TAG, "poseClassifierProcessor is null. Creating a new processor")
                        poseClassifierProcessor = PoseClassifierProcessor(context, true)
                    }
                    classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
                    Log.d(TAG, "Classification result $classificationResult")
                    PoseWithClassification(pose, classificationResult)
                }
            )
    }

    fun onFailure(e: Exception) {
        println("PoseDetectorProcessor on failure: $e")
    }
}