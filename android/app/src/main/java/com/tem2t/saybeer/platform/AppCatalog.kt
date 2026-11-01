package com.tem2t.saybeer.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo

data class InstalledApp(val packageName: String, val label: String)

object AppCatalog {
    fun protectedPackages(context: Context): Set<String> {
        val pm = context.packageManager
        val home = pm.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
        val dialer = context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage
        // Messaging/VoIP apps can handle ACTION_DIAL too. Protecting every handler hides
        // apps such as Zalo from the picker and also prevents the service from shielding them.
        // Only the actual default Phone app belongs in this safety allowlist.
        return protectedAppPackages(context.packageName, home, dialer)
    }
    fun load(context: Context): List<InstalledApp> {
        val protected = protectedPackages(context)
        return context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(context.packageManager).toString()) }
            .distinctBy { it.packageName }.filter { it.packageName !in protected }.sortedBy { it.label.lowercase() }
    }
    fun accessibilityEnabled(context: Context): Boolean = context.getSystemService(AccessibilityManager::class.java)
        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.packageName == context.packageName }

    fun openAccessibility(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

internal fun protectedAppPackages(own: String, home: String?, defaultDialer: String?): Set<String> =
    listOfNotNull(own, home, defaultDialer, "com.android.settings", "com.android.systemui",
        "com.android.phone", "com.android.emergency").toSet()
