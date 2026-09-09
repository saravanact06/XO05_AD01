package com.thambithappu.detection

sealed class ThreatEvent {
    object Clear : ThreatEvent()
    object Peek1Warning : ThreatEvent()
    object Peek2Protect : ThreatEvent()
    object Peek3Meme : ThreatEvent()
}

/**
 * Turns a stream of analyzed frames into discrete "someone is peeking" events, with
 * escalation across repeated peeks:
 *   1st sustained peek -> warning popup (Continue / Protect)
 *   2nd sustained peek -> stronger warning, protection applied automatically
 *   3rd+ sustained peek -> meme warning, protection applied automatically
 *
 * "Sustained" = the attentive-stranger condition must hold continuously for
 * [requiredPersistenceMs] before it counts as a peek - this is what stops someone
 * just walking past, or glancing over for a split second, from triggering anything.
 *
 * A peek is considered "over" once no attentive stranger has been seen for
 * [resolveAfterMs]. The escalation counter only advances on a NEW peek (i.e. after
 * the previous one has resolved), so one long peek doesn't rack up all three levels
 * in one go.
 */
class RiskEngine(
    private val ownerCalibration: OwnerCalibration = OwnerCalibration(),
    private val requiredPersistenceMs: Long = 200L,
    private val resolveAfterMs: Long = 500L
) {
    private var attentiveSinceMs: Long? = null
    private var peekActive = false
    private var lastAttentiveAtMs: Long = 0L
    private var peekCount = 0

    fun reset() {
        attentiveSinceMs = null
        peekActive = false
        peekCount = 0
    }

    /** Call once per analyzed frame with that frame's detected faces. */
    fun onFrame(
        faces: List<FaceData>,
        sensitivity: Float,
        nowMs: Long = System.currentTimeMillis()
    ): ThreatEvent {
        val (_, strangers) = ownerCalibration.classify(faces)
        val attentiveStranger = strangers.any { AttentionEngine.isAttentive(it, sensitivity) }

        if (attentiveStranger) {
            lastAttentiveAtMs = nowMs
            if (attentiveSinceMs == null) attentiveSinceMs = nowMs

            val sustainedFor = nowMs - (attentiveSinceMs ?: nowMs)
            if (!peekActive && sustainedFor >= requiredPersistenceMs) {
                peekActive = true
                peekCount++
                return when (peekCount) {
                    1 -> ThreatEvent.Peek1Warning
                    2 -> ThreatEvent.Peek2Protect
                    else -> ThreatEvent.Peek3Meme
                }
            }
        } else {
            attentiveSinceMs = null
            if (peekActive && nowMs - lastAttentiveAtMs >= resolveAfterMs) {
                peekActive = false
            }
        }

        return ThreatEvent.Clear
    }
}
