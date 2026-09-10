package com.thambithappu.detection

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

/**
 * Wraps ML Kit face detection as a CameraX ImageAnalysis.Analyzer.
 *
 * Privacy: frames are never written to disk. The optional Bitmap conversion in FrameResult
 * only happens in-memory, on demand, and should be discarded by the caller immediately
 * after use. Nothing here persists a frame.
 *
 * Battery: PERFORMANCE_MODE_FAST + frame skipping keep this cheap enough to run continuously
 * in the background without draining the device during a demo.
 */
class FaceDetectorAnalyzer(
    private val onFrameResult: (FrameResult) -> Unit,
    private val onError: (Exception) -> Unit = {},
    private val analyzeEveryNFrames: Int = 3 // tune this: lower = more responsive, higher = better battery
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // needed for eye-open probabilities
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)            // not needed, saves cycles
        .enableTracking()                                                  // gives stable trackingId -> use as faceId
        .build()

    private val detector = FaceDetection.getClient(options)
    private var frameCounter = 0

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        frameCounter++
        if (frameCounter % analyzeEveryNFrames != 0) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        val frameWidth = inputImage.width
        val frameHeight = inputImage.height
        val timestamp = System.currentTimeMillis()

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                val faceDataList = faces.map { it.toFaceData(frameWidth, frameHeight, timestamp) }

                // Convert to Bitmap HERE, synchronously, before imageProxy.close() runs below -
                // ImageProxy is invalid the moment it's closed, so a lazy closure over it would
                // crash or return garbage the first time something downstream actually calls it.
                // Only pay the YUV->RGB cost when there's more than one face - requires
                // camera-core 1.3+ for ImageProxy.toBitmap().
                //
                // IMPORTANT: imageProxy.toBitmap() returns the RAW sensor buffer, unrotated -
                // but the face bounding boxes above were computed against InputImage's
                // rotated width/height. Cropping the raw bitmap with those coordinates would
                // be misaligned, so rotate the bitmap here to match the same space the boxes
                // are in before handing it downstream (e.g. to MemeGenerator).
                val bitmap: android.graphics.Bitmap? =
                    if (faceDataList.size > 1) runCatching {
                        val raw = imageProxy.toBitmap()
                        if (rotation != 0) {
                            val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                            android.graphics.Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                        } else raw
                    }.getOrNull()
                    else null

                onFrameResult(FrameResult(faceDataList, frameWidth, frameHeight) { bitmap })
            }
            .addOnFailureListener { e -> onError(e) }
            .addOnCompleteListener {
                // Always close, success or failure, or CameraX stalls the whole pipeline.
                imageProxy.close()
            }
    }

    private fun Face.toFaceData(frameWidth: Int, frameHeight: Int, timestampMs: Long): FaceData {
        val box = boundingBox
        val relativeSize = (box.width().toFloat() * box.height().toFloat()) /
                (frameWidth.toFloat() * frameHeight.toFloat())

        return FaceData(
            faceId = trackingId ?: -1,
            boundingBoxLeft = box.left,
            boundingBoxTop = box.top,
            boundingBoxWidth = box.width(),
            boundingBoxHeight = box.height(),
            frameWidth = frameWidth,
            frameHeight = frameHeight,
            leftEyeOpenProbability = leftEyeOpenProbability,
            rightEyeOpenProbability = rightEyeOpenProbability,
            headYawDegrees = headEulerAngleY,
            headPitchDegrees = headEulerAngleX,
            headRollDegrees = headEulerAngleZ,
            relativeFaceSize = relativeSize,
            timestampMs = timestampMs
        )
    }
}