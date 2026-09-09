package com.thambithappu
import androidx.compose.foundation.layout.fillMaxSize
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thambithappu.camera.CameraScreen
import com.thambithappu.detection.FaceData
import com.thambithappu.detection.FrameResult
import com.thambithappu.detection.OwnerCalibration
import com.thambithappu.detection.RiskEngine
import com.thambithappu.detection.ThreatEvent

/**
 * Stands in for "a protected app" (chat, banking, whatever) for the demo. Runs the
 * camera + RiskEngine in the background and reacts according to [settings].
 *
 * Known limitation: this only demonstrates protection on THIS screen. Actually
 * detecting "which real app is in the foreground" (to honor the Protected Apps
 * list against other installed apps) needs an AccessibilityService or UsageStats,
 * which is out of scope for the hackathon build - worth calling out if asked.
 */
@Composable
fun ProtectedDemoScreen(
    settings: ProtectionSettings,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    val ownerCalibration = remember { OwnerCalibration() }
    val riskEngine = remember(settings.protectionEnabled) { RiskEngine(ownerCalibration) }

    var lastFaces by remember { mutableStateOf<List<FaceData>>(emptyList()) }
    var warningLevel by remember { mutableStateOf(0) }      // 0 = none, 1/2/3 = active levels
    var protectionApplied by remember { mutableStateOf(false) }

    // Reset UI state whenever protection is turned off, so re-enabling starts clean.
    LaunchedEffect(settings.protectionEnabled) {
        if (!settings.protectionEnabled) {
            warningLevel = 0
            protectionApplied = false
        }
    }

    fun applyProtection() {
        when (settings.mode) {
            ProtectionMode.FORCE_SWITCH -> goToHomeScreen(context) // fastest possible reaction
            ProtectionMode.BLUR, ProtectionMode.OVERLAY -> protectionApplied = true
            ProtectionMode.WARNING_ONLY -> Unit // warn only, never cover content
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        DemoSensitiveContent(
            blurred = protectionApplied && settings.mode == ProtectionMode.BLUR
        )

        if (protectionApplied && settings.mode == ProtectionMode.OVERLAY) {
            FullScreenProtectOverlay()
        }

        if (settings.protectionEnabled) {
            CameraScreen(
                modifier = Modifier.size(1.dp),
                onFrameResult = { frameResult: FrameResult ->
                    lastFaces = frameResult.faces
                    when (riskEngine.onFrame(frameResult.faces, settings.sensitivity)) {
                        ThreatEvent.Peek1Warning -> {
                            // Force Switch prioritizes speed over asking first.
                            if (settings.mode == ProtectionMode.FORCE_SWITCH) {
                                applyProtection()
                            } else {
                                warningLevel = 1
                            }
                        }
                        ThreatEvent.Peek2Protect -> {
                            warningLevel = 2
                            applyProtection()
                        }
                        ThreatEvent.Peek3Meme -> {
                            warningLevel = 3
                            applyProtection()
                        }
                        ThreatEvent.Clear -> Unit
                    }
                }
            )
        }

        // Top bar: exit + optional owner calibration, always reachable.
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallActionChip(text = "← Home") { onExit() }
            SmallActionChip(
                text = if (ownerCalibration.isCalibrated) "✅ Owner set" else "📸 Set Owner Face"
            ) { ownerCalibration.calibrate(lastFaces) }
        }

        if (warningLevel == 1) {
            PeekWarningDialog(
                title = "👀 Someone may be peeking into your phone",
                message = "A person appears to be looking at your screen.",
                onContinue = { warningLevel = 0 },
                onProtect = {
                    applyProtection()
                    warningLevel = 0
                }
            )
        }

        if (warningLevel == 2) {
            StrongWarningBanner(
                onDismiss = {
                    warningLevel = 0
                    protectionApplied = false
                }
            )
        }

        if (warningLevel == 3 && settings.thirdWarningMemeEnabled) {
            TamilMemeWarning(
                onDismiss = {
                    warningLevel = 0
                    protectionApplied = false
                }
            )
        }
    }
}

private fun goToHomeScreen(context: Context) {
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(homeIntent)
}
