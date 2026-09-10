package com.thambithappu

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.thambithappu.detection.FaceDetectorAnalyzer
import com.thambithappu.detection.OwnerCalibration
import com.thambithappu.detection.RiskEngine
import com.thambithappu.detection.ThreatEvent
import androidx.lifecycle.LifecycleService
import java.util.concurrent.Executors

/**
 * The ONE thing that runs protection - one camera session, works over any app including
 * our own, since the overlay is drawn on top of everything by the OS. Starting/stopping
 * this Service IS the on/off switch; there is no separate in-app toggle anymore.
 *
 * Also self-stops when the screen locks (ACTION_SCREEN_OFF) - the user has to turn
 * protection back on manually after unlocking, by design (per the spec: don't keep
 * running the camera against a locked, black screen).
 */
class ProtectionService : LifecycleService() {

    private val ownerCalibration = OwnerCalibration()
    private val riskEngine = RiskEngine(ownerCalibration)
    private lateinit var overlay: OverlayController
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor = Executors.newSingleThreadExecutor()

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProtectionServiceStatus.isRunning = true
        overlay = OverlayController(this)
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        startForeground(NOTIFICATION_ID, buildNotification())
        startCamera()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            cameraProvider = provider

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor, FaceDetectorAnalyzer(
                        analyzeEveryNFrames = 1,
                        onFrameResult = { frameResult ->
                            val settings = ProtectionSettingsHolder.current

                            val allowed = !settings.onlyProtectSelectedApps ||
                                ForegroundAppMonitor.isProtectedAppInForeground(
                                    this, settings.protectedApps
                                )

                            if (allowed) {
                                val result = riskEngine.onFrame(frameResult.faces, settings.sensitivity)
                                when (result.event) {
                                    ThreatEvent.Peek1Warning -> handlePeek(1, settings, frameResult, result.peekerFace)
                                    ThreatEvent.Peek2Protect -> handlePeek(2, settings, frameResult, result.peekerFace)
                                    ThreatEvent.Peek3Meme -> handlePeek(3, settings, frameResult, result.peekerFace)
                                    ThreatEvent.Clear -> Unit
                                }
                            }
                        }
                    ))
                }

            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, analysis)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handlePeek(
        level: Int,
        settings: ProtectionSettings,
        frameResult: com.thambithappu.detection.FrameResult,
        peekerFace: com.thambithappu.detection.FaceData?
    ) {
        if (settings.soundEnabled) playWarningSound()
        if (settings.vibrationEnabled) vibrate()

        if (settings.mode == ProtectionMode.FORCE_SWITCH) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            return
        }

        when (level) {
            1 -> overlay.showBanner(
                text = "👀 Someone may be peeking",
                backgroundColor = Color.parseColor("#FF8A7A")
            ) { overlay.hide() }

            2 -> {
                if (settings.mode == ProtectionMode.WARNING_ONLY) {
                    overlay.showBanner(
                        text = "🚨 Still peeking",
                        backgroundColor = Color.RED
                    ) { overlay.hide() }
                } else {
                    overlay.showFullScreenBlock(
                        title = "🔒 Screen protected",
                        message = "Someone kept looking - content hidden."
                    ) { overlay.hide() }
                }
            }

            else -> {
                // Generated fresh every time, from the actual current frame - not reused
                // from an earlier peek, so repeated peeking always gets a current photo.
                val meme = if (settings.funModeEnabled && peekerFace != null) {
                    frameResult.frameBitmapProvider()?.let { bitmap ->
                        runCatching { MemeGenerator.generate(bitmap, peekerFace) }
                            .onFailure { android.util.Log.e("ThambiThappu", "Meme generation failed", it) }
                            .getOrNull()
                    }
                } else null

                if (meme != null) {
                    overlay.showFullScreenImage(meme) { overlay.hide() }
                } else if (settings.mode != ProtectionMode.WARNING_ONLY) {
                    overlay.showFullScreenBlock(
                        title = "🔒 Screen protected",
                        message = "Repeated peeking detected."
                    ) { overlay.hide() }
                }
            }
        }
    }

    private fun playWarningSound() {
        runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.play()
        }
    }

    private fun vibrate() {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "thambi_thappu_protection"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Background protection", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Thambi Thappu is watching")
            .setContentText("Protecting your screen - turns off automatically when you lock your phone")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        ProtectionServiceStatus.isRunning = false
        runCatching { unregisterReceiver(screenOffReceiver) }
        overlay.hide()
        cameraProvider?.unbindAll()
        executor.shutdown()
    }

    companion object {
        private const val NOTIFICATION_ID = 42
    }
}
