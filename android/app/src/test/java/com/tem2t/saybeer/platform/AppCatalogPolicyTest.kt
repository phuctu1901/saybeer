package com.tem2t.saybeer.platform

import org.junit.Assert.*
import org.junit.Test

class AppCatalogPolicyTest {
    @Test fun messagingAppsRemainSelectableWhileRealPhoneIsProtected() {
        val protected = protectedAppPackages("com.tem2t.saybeer", "example.home", "com.google.android.dialer")
        assertFalse("Zalo must remain selectable", "com.zing.zalo" in protected)
        assertFalse("A generic VoIP dial handler must remain selectable", "example.voip" in protected)
        assertTrue("com.google.android.dialer" in protected)
        assertTrue("example.home" in protected)
        assertTrue("com.android.settings" in protected)
        assertTrue("com.tem2t.saybeer" in protected)
    }
    @Test fun thirdPartyDefaultDialerIsStillProtected() {
        assertTrue("example.phone" in protectedAppPackages("own", null, "example.phone"))
    }
    @Test fun devicesWithoutDefaultDialerStillProtectEmergencyAndSystemUi() {
        val protected = protectedAppPackages("own", null, null)
        assertTrue("com.android.phone" in protected)
        assertTrue("com.android.emergency" in protected)
        assertTrue("com.android.systemui" in protected)
        assertEquals(5, protected.size)
    }
}
