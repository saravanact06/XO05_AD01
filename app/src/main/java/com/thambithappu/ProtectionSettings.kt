package com.thambithappu

/**
 * Single source of truth for what protection currently means. Hoisted in MainActivity
 * and mirrored into ProtectionSettingsHolder so ProtectionService (a different lifecycle
 * entirely) can read the same values.
 *
 * There is deliberately no separate "protectionEnabled" flag here - whether protection is
 * on IS whether ProtectionService is running. One switch, one source of truth.
 */
enum class ProtectionMode {
    WARNING_ONLY,
    BLUR,
    OVERLAY,
    FORCE_SWITCH
}

data class ProtectionSettings(
    val mode: ProtectionMode = ProtectionMode.BLUR,
    val sensitivity: Float = 0.6f,          // 0f (least sensitive) .. 1f (most sensitive)
    val protectedApps: Set<String> = setOf("WhatsApp", "Banking"),
    val onlyProtectSelectedApps: Boolean = false, // false = protect everywhere; true = only the apps above
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val funModeEnabled: Boolean = false     // teases the peeker with their own cropped photo on the 3rd warning
)