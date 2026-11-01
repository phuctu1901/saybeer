package com.tem2t.saybeer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tem2t.saybeer.data.SessionRepository
import com.tem2t.saybeer.ui.SayBeerApp
import com.tem2t.saybeer.ui.SayBeerTheme
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingTest {
    @get:Rule val compose = createComposeRule()
    @Test fun onboardingExplainsPermissionBeforeOpeningSettings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SessionRepository(context)
        repository.update { it.copy(onboarded = false) }
        compose.setContent { SayBeerTheme { SayBeerApp(repository) } }
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Thiết lập quyền & bắt đầu").performClick()
        compose.onNodeWithText("Cho phép SayBeer khóa app?").assertIsDisplayed()
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = java.io.File(context.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        java.io.File(folder, "11-permission.png").outputStream().use {
            instrumentation.uiAutomation.takeScreenshot()!!.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText("Để sau").performClick()
        compose.onNodeWithText("Cho phép SayBeer khóa app?").assertDoesNotExist()
        org.junit.Assert.assertTrue(repository.current.onboarded)
        compose.onNodeWithText("SAYBEER").assertIsDisplayed()
    }
}
