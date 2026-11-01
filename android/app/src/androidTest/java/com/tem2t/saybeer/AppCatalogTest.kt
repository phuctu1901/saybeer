package com.tem2t.saybeer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tem2t.saybeer.platform.AppCatalog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

// Lives only in the test APK. Simulates a messaging app that also accepts phone links.
class DialHandlerFixtureActivity : Activity()

@RunWith(AndroidJUnit4::class)
class AppCatalogTest {
    @Test fun launcherAppWithDialHandlerIsVisibleAndNotProtected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val target = instrumentation.targetContext
        val fixture = instrumentation.context.packageName
        val handlers = instrumentation.context.packageManager.queryIntentActivities(Intent(Intent.ACTION_DIAL, Uri.parse("tel:123")), 0)
        assertTrue("Fixture must reproduce the dial-handler case", handlers.any { it.activityInfo.packageName == fixture })
        assertFalse("A dial intent handler is not the default Phone app", fixture in AppCatalog.protectedPackages(target))
        assertTrue("Messaging apps must appear in the real picker catalog", AppCatalog.load(target).any { it.packageName == fixture })
    }
}
