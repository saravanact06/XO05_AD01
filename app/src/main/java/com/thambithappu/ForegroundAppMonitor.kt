package com.thambithappu

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process

/**
 * Tells you which app is currently in the foreground, so protection can be limited to
 * only the apps the user picked (WhatsApp, banking, etc.) instead of running everywhere.
 *
 * "Which app is in front" isn't something a normal app is allowed to know for privacy
 * reasons - it needs PACKAGE_USAGE_STATS, a special permission granted only through a
 * Settings screen (same shape as the overlay permission), never a runtime dialog.
 *
 * Known limitation: "WhatsApp"/"Banking"/"Gallery"/"Email" in the UI are display labels,
 * not real package names - there's no single universal "the banking app." labelToPackage
 * below is a best-effort mapping for common apps; swap in whatever's actually installed
 * on your test device for anything that doesn't match.
 */
object ForegroundAppMonitor {

    private val labelToPackage = mapOf(
        "WhatsApp" to "com.whatsapp",
        "Email" to "com.google.android.gm",
        "Gallery" to "com.google.android.apps.photos"
        // "Banking" intentionally omitted - no generic package exists; replace with your
        // actual banking app's package name if you want to protect it specifically.
    )

    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Returns the package name of whatever app was frontmost in roughly the last second. */
    fun currentForegroundPackage(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 2000L
        val events = usm.queryEvents(start, end)
        var lastResumedPackage: String? = null
        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastResumedPackage = event.packageName
            }
        }
        return lastResumedPackage
    }

    /** True if the current foreground app is one the user selected in Protected Apps. */
    fun isProtectedAppInForeground(context: Context, selectedLabels: Set<String>): Boolean {
        val foregroundPackage = currentForegroundPackage(context) ?: return false
        val protectedPackages = selectedLabels.mapNotNull { labelToPackage[it] }.toSet()
        return foregroundPackage in protectedPackages || foregroundPackage == context.packageName
    }
}
