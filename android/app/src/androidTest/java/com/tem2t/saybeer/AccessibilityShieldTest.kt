package com.tem2t.saybeer

import android.app.UiAutomation
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tem2t.saybeer.data.*
import com.tem2t.saybeer.platform.AppCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

/** Enables the real service on the disposable emulator, then restores the prior settings. */
@RunWith(AndroidJUnit4::class)
class AccessibilityShieldTest {
    @Test fun selectedAppIsShieldedWorkWindowReleasesAndThenReblocks() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        fun shell(command: String) { automation.executeShellCommand(command).use { FileInputStream(it.fileDescriptor).readBytes() } }
        val original = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val service = "${context.packageName}/.platform.BeerAccessibilityService"
        // Instrumentation force-stops the target process. Rebind only this app's existing test
        // service, preserving every other service exactly as found on the emulator.
        val others = original.split(':').filter { it.isNotBlank() && !it.startsWith("${context.packageName}/") }.joinToString(":")
        val enabled = (others.split(':').filter(String::isNotBlank) + service).joinToString(":")
        val apps = AppCatalog.load(context)
        val app = apps.firstOrNull { it.packageName == "com.android.chrome" } ?: apps.first()
        val repo = context.repository
        fun waitFor(condition: () -> Boolean) {
            val deadline = SystemClock.elapsedRealtime() + 10_000
            while (!condition() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(100)
            assertTrue("Service state did not converge", condition())
        }
        fun shieldVisible() = automation.rootInActiveWindow?.findAccessibilityNodeInfosByText("Để sáng mai không phải xin lỗi.")?.isNotEmpty() == true
        try {
            shell("settings put secure enabled_accessibility_services $others")
            SystemClock.sleep(500)
            shell("settings put secure enabled_accessibility_services $enabled")
            shell("settings put secure accessibility_enabled 1")
            waitFor { AppCatalog.accessibilityEnabled(context) }
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            waitFor { automation.rootInActiveWindow?.packageName?.toString() == context.packageName }
            val now = System.currentTimeMillis()
            instrumentation.runOnMainSync { repo.update { BeerState(onboarded = true, selected = setOf(app.packageName), session = Session(now, now + 240 * MINUTE)) } }
            context.startActivity(context.packageManager.getLaunchIntentForPackage(app.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            waitFor { shieldVisible() }
            val folder = File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
            File(folder, "09-shield.png").outputStream().use { automation.takeScreenshot()!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
            instrumentation.runOnMainSync { repo.openWindow() }
            waitFor { !shieldVisible() }
            instrumentation.runOnMainSync { repo.update { it.copy(session = it.session!!.copy(windowEnd = System.currentTimeMillis() + 1500)) } }
            waitFor { shieldVisible() }
            instrumentation.runOnMainSync { repo.finish("emergency") }
            waitFor { !shieldVisible() }
        } finally {
            instrumentation.runOnMainSync { repo.update { BeerState() } }
            if (original.isBlank()) shell("settings delete secure enabled_accessibility_services")
            else shell("settings put secure enabled_accessibility_services $original")
            shell("settings put secure accessibility_enabled ${if (original.isBlank()) 0 else 1}")
        }
    }
}
