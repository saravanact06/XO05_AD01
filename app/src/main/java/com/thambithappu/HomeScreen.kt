package com.thambithappu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private val Paper = Color(0xFFFFFBF2)
private val Ink = Color(0xFF292522)
private val Coral = Color(0xFFFF8A7A)
private val Yellow = Color(0xFFFFD966)
private val Mint = Color(0xFFB8E6D2)
private val Lavender = Color(0xFFD9CCFF)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    settings: ProtectionSettings,
    onSettingsChange: (ProtectionSettings) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var serviceRunning by remember { mutableStateOf(ProtectionServiceStatus.isRunning) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    fun hasOverlayPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    fun startProtection() {
        ContextCompat.startForegroundService(context, Intent(context, ProtectionService::class.java))
        serviceRunning = true
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && hasOverlayPermission()) startProtection()
    }

    // The service can stop itself (screen lock) without us knowing - re-sync the switch
    // to reality every time this screen comes back into view.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceRunning = ProtectionServiceStatus.isRunning
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun attemptStart() {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        if (!hasOverlayPermission()) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            )
            return
        }
        startProtection()
    }

    fun stopProtection() {
        context.stopService(Intent(context, ProtectionService::class.java))
        serviceRunning = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {

        DecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "THAMBI", color = Ink, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Text(text = "THAPPU!", color = Coral, fontSize = 37.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "Don't peek. It's thambi thappu.", color = Ink.copy(alpha = 0.65f), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(22.dp))

            PhoneIllustration()

            Spacer(modifier = Modifier.height(20.dp))

            // PRIVACY PROTECTION - the one switch that runs everything (was two before)
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Privacy Protection", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (serviceRunning) "Watching - works across any app" else "Currently off",
                            fontSize = 13.sp,
                            color = Ink.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = serviceRunning,
                        onCheckedChange = { turnOn -> if (turnOn) attemptStart() else stopProtection() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PROTECTED APPS
            AppCard {
                SectionTitle(emoji = "📱", title = "Protected Apps")
                Text(
                    text = "Choose where Thambi Thappu should protect you.",
                    color = Ink.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppChoice(name = "WhatsApp", emoji = "💬", selected = settings.protectedApps.contains("WhatsApp")) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "WhatsApp")))
                }
                AppChoice(name = "Banking", emoji = "🏦", selected = settings.protectedApps.contains("Banking")) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Banking")))
                }
                AppChoice(name = "Gallery", emoji = "🖼️", selected = settings.protectedApps.contains("Gallery")) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Gallery")))
                }
                AppChoice(name = "Email", emoji = "✉️", selected = settings.protectedApps.contains("Email")) {
                    onSettingsChange(settings.copy(protectedApps = toggleApp(settings.protectedApps, "Email")))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Only protect selected apps", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
                        Text(
                            text = "Off = protect everywhere. On = only the apps above.",
                            fontSize = 11.sp,
                            color = Ink.copy(alpha = 0.55f)
                        )
                    }
                    Switch(
                        checked = settings.onlyProtectSelectedApps,
                        onCheckedChange = { turnOn ->
                            if (turnOn && !ForegroundAppMonitor.hasPermission(context)) {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } else {
                                onSettingsChange(settings.copy(onlyProtectSelectedApps = turnOn))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PROTECTION MODES
            AppCard {
                SectionTitle(emoji = "🛡️", title = "Protection Mode")
                Text(text = "Choose what happens when someone peeks.", color = Ink.copy(alpha = 0.6f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                ModeRow(
                    title = "Warning only", description = "Show a warning, don't hide anything",
                    selected = settings.mode == ProtectionMode.WARNING_ONLY
                ) { onSettingsChange(settings.copy(mode = ProtectionMode.WARNING_ONLY)) }

                ModeRow(
                    title = "Blur / Protect", description = "Cover the screen, with a warning icon",
                    selected = settings.mode == ProtectionMode.BLUR
                ) { onSettingsChange(settings.copy(mode = ProtectionMode.BLUR)) }

                ModeRow(
                    title = "Privacy Overlay", description = "Cover the screen entirely (opaque)",
                    selected = settings.mode == ProtectionMode.OVERLAY
                ) { onSettingsChange(settings.copy(mode = ProtectionMode.OVERLAY)) }

                ModeRow(
                    title = "Force Switch", description = "Instantly jump to the home screen - fastest reaction",
                    selected = settings.mode == ProtectionMode.FORCE_SWITCH
                ) { onSettingsChange(settings.copy(mode = ProtectionMode.FORCE_SWITCH)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SENSITIVITY
            AppCard {
                SectionTitle(emoji = "🎯", title = "Detection Sensitivity")
                Text(
                    text = when {
                        settings.sensitivity < 0.35f -> "Low"
                        settings.sensitivity < 0.7f -> "Medium"
                        else -> "High"
                    },
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Coral
                )
                Slider(
                    value = settings.sensitivity,
                    onValueChange = { onSettingsChange(settings.copy(sensitivity = it)) },
                    valueRange = 0f..1f
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Less sensitive", fontSize = 12.sp, color = Ink.copy(alpha = 0.5f))
                    Text(text = "More sensitive", fontSize = 12.sp, color = Ink.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // FUN MODE - separate from Quick Settings on purpose, since it's the standout feature
            AppCard {
                SectionTitle(emoji = "🎭", title = "Fun Mode")
                Text(
                    text = "On the 3rd warning, tease the peeker with their own cropped photo instead of a plain message.",
                    color = Ink.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (settings.funModeEnabled) "Enabled" else "Off",
                        fontSize = 13.sp,
                        color = Ink.copy(alpha = 0.6f)
                    )
                    Switch(
                        checked = settings.funModeEnabled,
                        onCheckedChange = { onSettingsChange(settings.copy(funModeEnabled = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // QUICK SETTINGS
            AppCard {
                SectionTitle(emoji = "⚙️", title = "Quick Settings")
                SettingRow(title = "Warning sound", enabled = settings.soundEnabled) {
                    onSettingsChange(settings.copy(soundEnabled = it))
                }
                SettingRow(title = "Vibration", enabled = settings.vibrationEnabled) {
                    onSettingsChange(settings.copy(vibrationEnabled = it))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Your screen. Your privacy.", color = Ink.copy(alpha = 0.45f), fontSize = 13.sp)

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Overflow menu - app description / how-to-use, replacing the old inline escalation card
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 12.dp)) {
            Text(
                text = "⋮",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { menuExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("About & how to use") },
                    onClick = {
                        menuExpanded = false
                        showAboutDialog = true
                    }
                )
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                confirmButton = {
                    Text(
                        text = "Got it",
                        fontWeight = FontWeight.Bold,
                        color = Coral,
                        modifier = Modifier.clickable { showAboutDialog = false }.padding(8.dp)
                    )
                },
                title = { Text("About Thambi Thappu") },
                text = {
                    Column {
                        Text(
                            "Watches for someone else looking at your screen using your front " +
                                    "camera, and reacts in three escalating steps."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1st peek: a warning. 2nd peek: a stronger warning and your screen is automatically protected. 3rd peek: a funny warning - or, with Fun Mode on, a teasing photo of the peeker.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("How to use: flip Privacy Protection on, grant the camera and \"draw over other apps\" permissions when asked, then use your phone as normal - protection runs across any app in the background, and turns off automatically when you lock your phone.")
                    }
                }
            )
        }
    }
}

@Composable
private fun AppCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(2.dp, Ink, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(18.dp)) { content() }
    }
}

@Composable
private fun SectionTitle(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun AppChoice(name: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(
            text = if (selected) "✓" else "○",
            color = if (selected) Color(0xFF32805B) else Ink.copy(alpha = 0.35f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeRow(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = if (selected) "●" else "○", color = if (selected) Coral else Ink.copy(alpha = 0.4f), fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, color = Ink)
            Text(text = description, fontSize = 12.sp, color = Ink.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun SettingRow(title: String, enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f), color = Ink, fontSize = 14.sp)
        Switch(checked = enabled, onCheckedChange = onCheckedChange)
    }
}

private fun toggleApp(apps: Set<String>, app: String): Set<String> =
    if (apps.contains(app)) apps - app else apps + app

@Composable
private fun PhoneIllustration() {
    Box(modifier = Modifier.size(width = 180.dp, height = 150.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(width = 90.dp, height = 145.dp)
                .clip(RoundedCornerShape(20.dp)).background(Ink).padding(6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(15.dp)).background(Mint),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PRIVATE", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "🔒", fontSize = 30.sp)
                }
            }
        }
        Box(
            modifier = Modifier.size(62.dp).align(Alignment.TopEnd).clip(CircleShape)
                .background(Coral).border(3.dp, Ink, CircleShape)
        ) {
            Row(modifier = Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Ink))
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Ink))
            }
        }
        Text(text = "👀", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopStart).padding(top = 15.dp))
    }
}

@Composable
private fun DecorativeBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = Yellow.copy(alpha = 0.45f), radius = 70f, center = Offset(size.width * 0.9f, size.height * 0.08f))
        drawCircle(color = Lavender.copy(alpha = 0.45f), radius = 55f, center = Offset(size.width * 0.05f, size.height * 0.75f))
        drawLine(
            color = Coral,
            start = Offset(size.width * 0.08f, size.height * 0.14f),
            end = Offset(size.width * 0.17f, size.height * 0.10f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}