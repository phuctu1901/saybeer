package com.tem2t.saybeer

import android.app.UiAutomation
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.tem2t.saybeer.data.BeerState
import com.tem2t.saybeer.data.MINUTE
import com.tem2t.saybeer.ui.SayBeerApp
import com.tem2t.saybeer.ui.SayBeerTheme
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileInputStream

/** Captures real Compose screens with the actual accessibility permission enabled on the emulator. */
class StoreScreenshotsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun captureStoreScreens() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        val original = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        fun shell(command: String) { automation.executeShellCommand(command).use { FileInputStream(it.fileDescriptor).readBytes() } }
        val service = "${context.packageName}/.platform.BeerAccessibilityService"
        val others = original.split(':').filter { it.isNotBlank() && !it.startsWith("${context.packageName}/") }.joinToString(":")
        val services = (others.split(':').filter(String::isNotBlank) + service).joinToString(":")
        fun shot(name: String) {
            compose.waitForIdle()
            val folder = File(context.getExternalFilesDir(null), "store-screenshots").apply { mkdirs() }
            File(folder, "$name.png").outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        try {
            // Rebind only SayBeer after instrumentation force-stops the app, retaining other services.
            shell("settings put secure enabled_accessibility_services $others")
            android.os.SystemClock.sleep(500)
            shell("settings put secure enabled_accessibility_services $services")
            shell("settings put secure accessibility_enabled 1")
            context.repository.update { BeerState(onboarded = true, selected = setOf("com.android.chrome")) }
            var keyboard: androidx.compose.ui.platform.SoftwareKeyboardController? = null
            compose.setContent { SayBeerTheme { keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current; SayBeerApp(context.repository) } }
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Quyền khóa app chưa được bật").fetchSemanticsNodes().isEmpty() }
            shot("01-home")
            compose.onNodeWithText("Bắt đầu chế độ nhậu").performScrollTo().performClick()
            shot("02-start")
            compose.onNodeWithText("Chọn app cần khóa", substring = true).performClick()
            compose.onNodeWithText("Tìm tên ứng dụng").performTextInput("Chrome")
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Chrome", substring = false).fetchSemanticsNodes().isNotEmpty() }
            // Clear keyboard focus through the ordinary Android back action before capture.
            compose.runOnIdle { keyboard?.hide() }
            shot("03-app-picker")
            compose.onNodeWithText("Lưu (1)").performClick()
            compose.onNodeWithContentDescription("Quay lại").performClick()
            compose.runOnIdle { context.repository.start(System.currentTimeMillis() + 240 * MINUTE) }
            compose.onNodeWithText("Đang khóa 1 app").assertExists()
            shot("04-active")
            compose.onNodeWithText("Cần nhắn việc", substring = true).performScrollTo().performClick()
            shot("05-work")
            compose.onNodeWithContentDescription("Quay lại").performClick()
            compose.onNodeWithText("Tỉnh rồi — mở khóa hẳn").performScrollTo().performClick()
            shot("06-test")
            compose.onNodeWithContentDescription("Quay lại").performClick()
            compose.onNodeWithText("SOS  Khẩn cấp").performScrollTo().performClick()
            shot("07-emergency")
            compose.onNodeWithContentDescription("Quay lại").performClick()
            compose.runOnIdle { context.repository.finish("emergency") }
            compose.onNodeWithText("Hẹn lịch").performScrollTo().performClick()
            compose.onNode(isToggleable()).performClick()
            compose.onNodeWithText("T6").performClick()
            compose.onNodeWithText("T7").performClick()
            shot("08-schedule")
        } finally {
            context.repository.update { BeerState() }
            if (original.isBlank()) shell("settings delete secure enabled_accessibility_services")
            else shell("settings put secure enabled_accessibility_services $original")
            shell("settings put secure accessibility_enabled ${if (original.isBlank()) 0 else 1}")
        }
    }
}
