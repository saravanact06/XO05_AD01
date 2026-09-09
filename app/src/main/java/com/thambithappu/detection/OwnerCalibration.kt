package com.thambithappu.detection

/**
 * MVP "owner recognition": this does NOT run real face-recognition/embeddings -
 * that needs a trained model (e.g. FaceNet/MobileFaceNet in TFLite) and time to
 * validate, which a 1-hour build doesn't have. Instead this uses a distance-based
 * heuristic that holds for the overwhelming majority of shoulder-surfing scenarios:
 * the owner is holding the phone, so the owner's face is the closest (= largest
 * bounding box) face in frame. Anyone else in frame is, by definition, further
 * away, and is treated as a potential peeker.
 *
 * Works out of the box with zero setup (falls back to "largest face = owner").
 * Calling [calibrate] once (e.g. from a "Set Owner Face" button while the camera
 * is running) records how large the owner's face typically is at normal holding
 * distance, so a stranger who leans in very close isn't misread as the owner just
 * because they're momentarily the biggest face in frame.
 */
class OwnerCalibration {

    var ownerReferenceRelativeSize: Float? = null
        private set

    val isCalibrated: Boolean get() = ownerReferenceRelativeSize != null

    fun calibrate(faces: List<FaceData>) {
        val largest = faces.maxByOrNull { it.relativeFaceSize } ?: return
        ownerReferenceRelativeSize = largest.relativeFaceSize
    }

    fun reset() {
        ownerReferenceRelativeSize = null
    }

    /**
     * Splits one frame's faces into (owner, potentialStrangers).
     * Uses reference equality (`!==`) to build the stranger list, not faceId
     * comparison - ML Kit's trackingId can be null (-> -1) for more than one
     * face in the same frame if tracking briefly fails, and comparing on faceId
     * alone would then wrongly treat every -1 face as "the owner".
     */
    fun classify(faces: List<FaceData>): Pair<FaceData?, List<FaceData>> {
        if (faces.isEmpty()) return null to emptyList()
        if (faces.size == 1) return faces[0] to emptyList()

        val refSize = ownerReferenceRelativeSize
        val owner = if (refSize != null) {
            faces.minByOrNull { kotlin.math.abs(it.relativeFaceSize - refSize) }
        } else {
            faces.maxByOrNull { it.relativeFaceSize }
        }
        val strangers = faces.filter { it !== owner }
        return owner to strangers
    }
}
