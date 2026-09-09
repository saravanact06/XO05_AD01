package com.thambithappu.detection

/**
 * Framework-agnostic snapshot of one detected face for one analyzed frame.
 *
 * Produced by FaceDetectorAnalyzer. Consumed by:
 *  - OwnerCalibration (decides owner vs stranger)
 *  - AttentionEngine (decides "is this face looking at the screen")
 *  - RiskEngine (combines the two into escalating peek events)
 *
 * Deliberately does NOT include isPrimary/ownerMatch - raw detection has no way
 * to know who a face belongs to. That's computed downstream, after classification
 * runs on the face list for the frame.
 */
data class FaceData(
    val faceId: Int,                     // ML Kit tracking ID - stable across frames for the same physical face
    val boundingBoxLeft: Int,
    val boundingBoxTop: Int,
    val boundingBoxWidth: Int,
    val boundingBoxHeight: Int,
    val frameWidth: Int,
    val frameHeight: Int,
    val leftEyeOpenProbability: Float?,  // null when classification didn't run for this face
    val rightEyeOpenProbability: Float?,
    val headYawDegrees: Float,           // Euler Y - left/right turn. Most reliable of the three.
    val headPitchDegrees: Float,         // Euler X - up/down tilt. Less reliable on some devices - treat as advisory.
    val headRollDegrees: Float,          // Euler Z - sideways head tilt
    val relativeFaceSize: Float,         // (boxWidth * boxHeight) / (frameWidth * frameHeight) -> proximity proxy
    val timestampMs: Long
)

/**
 * Emitted alongside a frame's FaceData list so downstream code can crop and use a
 * specific face WITHOUT this module needing to know anything about embeddings.
 * frameBitmapProvider is lazy - only converts YUV -> Bitmap if something downstream
 * actually calls it, since that conversion isn't free and we don't want to pay for
 * it every single frame.
 */
class FrameResult(
    val faces: List<FaceData>,
    val frameWidth: Int,
    val frameHeight: Int,
    val frameBitmapProvider: () -> android.graphics.Bitmap?
)
