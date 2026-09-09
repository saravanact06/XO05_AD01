package com.thambithappu

/**
 * Single source of truth for what "Enable Protection" currently means.
 * Hoisted out of HomeScreen so both the settings UI and the live monitoring
 * screen (ProtectedDemoScreen) read/write the exact same state.
 */
enum class ProtectionMode {
    WARNING_ONLY,
    BLUR,
    OVERLAY,
    FORCE_SWITCH
}

data class ProtectionSettings(
    val protectionEnabled: Boolean = false,
    val mode: ProtectionMode = ProtectionMode.BLUR,
    val sensitivity: Float = 0.6f,          // 0f (least sensitive) .. 1f (most sensitive)
    val protectedApps: Set<String> = setOf("WhatsApp", "Banking"),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val thirdWarningMemeEnabled: Boolean = true
)
