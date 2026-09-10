package com.thambithappu

/**
 * The Service and the Compose UI live in different lifecycles and can't share Compose
 * state directly, so this tiny in-memory holder is how the Service reads whatever
 * mode / sensitivity / meme-toggle the user last set in HomeScreen. Not persisted across
 * process death - same "session only" limitation as the rest of ProtectionSettings.
 */
object ProtectionSettingsHolder {
    @Volatile
    var current: ProtectionSettings = ProtectionSettings()
}
