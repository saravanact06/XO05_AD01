package com.thambithappu.detection

/**
 * Approximates "is this (stranger) face looking at the screen" from what ML Kit's
 * Face Detection API actually gives us - head-pose Euler angles and eye-open
 * probabilities. This is NOT true gaze tracking (that needs iris landmarks or a
 * dedicated gaze model). It's a practical stand-in: a face that is turned toward
 * the camera, with both eyes open, and close enough to plausibly read the screen,
 * counts as "attentive". Be upfront about this distinction if you present it.
 *
 * This is also what stops someone merely walking past from triggering anything:
 * they're usually in profile (high yaw) relative to the camera, not facing it.
 *
 * sensitivity (0f..1f, straight from the Detection Sensitivity slider) widens or
 * narrows the yaw/pitch tolerance and the minimum face size required to count.
 */
object AttentionEngine {

    fun isAttentive(face: FaceData, sensitivity: Float): Boolean {
        val s = sensitivity.coerceIn(0f, 1f)

        // Higher sensitivity -> tolerate more head turn and accept smaller/farther faces.
        val maxYawDegrees = 20f + (s * 25f)              // 20°..45°
        val maxPitchDegrees = 20f + (s * 20f)             // 20°..40°
        val minRelativeFaceSize = 0.015f - (s * 0.010f)   // stricter (bigger) at low sensitivity

        val facingCamera = kotlin.math.abs(face.headYawDegrees) <= maxYawDegrees &&
            kotlin.math.abs(face.headPitchDegrees) <= maxPitchDegrees

        val closeEnough = face.relativeFaceSize >= minRelativeFaceSize

        // Null probability (classification didn't run for this face) -> don't let a
        // missing eye reading block detection; treat as "open" rather than "closed".
        val eyesOpen = (face.leftEyeOpenProbability ?: 1f) >= 0.4f &&
            (face.rightEyeOpenProbability ?: 1f) >= 0.4f

        return facingCamera && closeEnough && eyesOpen
    }
}
