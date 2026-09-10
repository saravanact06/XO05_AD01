package com.thambithappu

/**
 * The service can stop itself (screen lock, user swipes away the notification, etc.)
 * without the UI knowing - this is how HomeScreen checks the real current state instead
 * of trusting whatever it last set.
 */
object ProtectionServiceStatus {
    @Volatile
    var isRunning: Boolean = false
}
