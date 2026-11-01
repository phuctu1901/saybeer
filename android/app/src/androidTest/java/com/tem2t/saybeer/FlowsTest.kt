package com.tem2t.saybeer

import androidx.compose.runtime.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tem2t.saybeer.data.*
import com.tem2t.saybeer.platform.AchievementShare
import com.tem2t.saybeer.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlowsTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repo get() = context.repository
    private val now get() = System.currentTimeMillis()
    private fun prepare(active: Boolean = false, math: Boolean = false) {
        repo.update { BeerState(onboarded = true, selected = setOf("com.android.chrome"), math = math,
            session = if (active) Session(now, now + 240 * MINUTE, math = math) else null) }
    }

    @Test fun startSessionUsesSelectionAndPersists() {
        prepare()
        compose.setContent { SayBeerTheme { val state by repo.state.collectAsState(); StartScreen(repo, state, true, true, {}, {}, {}, {}, {}) } }
        compose.onNodeWithText("Kích hoạt chế độ nhậu").performClick()
        assertNotNull(repo.current.session)
        assertEquals(repo.current.session, SessionRepository(context).current.session)
    }
    @Test fun workWindowRequiresExactCommitmentAndWait() {
        prepare(active = true)
        var clock by mutableLongStateOf(now)
        compose.setContent { SayBeerTheme { val state by repo.state.collectAsState(); WorkScreen(repo, state, clock) {} } }
        compose.onNodeWithContentDescription("Quay lại").assertIsDisplayed()
        compose.onNodeWithText("Gõ lời cam kết để tiếp tục").assertIsNotEnabled()
        compose.onNodeWithText("Gõ lại câu trên…").performTextInput("chỉ nhắn việc, không tâm sự")
        compose.onNodeWithText("Hít thở đã… 15s").assertIsNotEnabled()
        compose.runOnIdle { clock = now + 16_000 }
        compose.onNodeWithText("Mở cửa sổ nhắn việc").performClick()
        assertEquals(1, repo.current.session!!.windowsUsed)
        assertTrue(repo.current.session!!.windowEnd > now)
    }
    @Test fun failedSobrietyTestLocksOutAfterThreeAttempts() {
        prepare(active = true)
        compose.setContent { SayBeerTheme { val state by repo.state.collectAsState(); TestScreen(repo, state, now) {} } }
        repeat(3) {
            compose.onNodeWithText("Gõ lại câu trên…").performTextInput("sai")
            compose.onNodeWithText("Mở khóa").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Sai 3 lần rồi").assertIsDisplayed()
        assertTrue(repo.current.session!!.lockoutUntil > now)
    }
    @Test fun emergencyReleaseCancelsAndAccessibleCountdownFinishes() {
        prepare(active = true)
        compose.setContent { SayBeerTheme { EmergencyScreen(repo) {} } }
        val button = compose.onNodeWithContentDescription("Giữ 5 giây để mở khóa khẩn cấp")
        button.performTouchInput { down(center); advanceEventTime(100); up() }
        assertNotNull(repo.current.session)
        // Drive the LaunchedEffect countdown with Compose time, not an emulator-speed timeout.
        compose.mainClock.autoAdvance = false
        try {
            button.performSemanticsAction(SemanticsActions.OnLongClick) { it() }
            compose.mainClock.advanceTimeBy(4_000)
            compose.runOnIdle { assertNotNull(repo.current.session) }
            compose.mainClock.advanceTimeBy(1_200)
            compose.runOnIdle { assertNull(repo.current.session) }
        } finally {
            compose.mainClock.autoAdvance = true
        }
        assertEquals("emergency", repo.current.history.last().reason)
    }
    @Test fun weeklyScheduleSavesOvernightConfiguration() {
        prepare()
        repo.update { it.copy(schedule = WeeklySchedule(true, setOf(5, 6), 1080, 720)) }
        compose.setContent { SayBeerTheme { val state by repo.state.collectAsState(); ScheduleScreen(repo, state, true, true, {}, {}, {}, {}) } }
        compose.onNodeWithText("Lưu lịch").performClick()
        assertEquals(720, SessionRepository(context).current.schedule.durationMinutes)
        assertEquals(setOf(5, 6), repo.current.schedule.days)
    }
    @Test fun statisticsAndShareCardRender() {
        prepare()
        repo.update { it.copy(history = (1..3).map { Record(now - it * 24 * 60 * MINUTE, now, now - (it * 24 * 60 - 240) * MINUTE, "done", 1) }) }
        compose.setContent { SayBeerTheme { StatsScreen(repo.current) {} } }
        compose.onNodeWithText("Phiên an toàn").assertIsDisplayed()
        val uri = AchievementShare.create(context, repo.current)
        val bitmap = context.contentResolver.openInputStream(uri).use { android.graphics.BitmapFactory.decodeStream(it) }
        assertEquals(1080, bitmap.width); assertEquals(1350, bitmap.height); bitmap.recycle()
    }
    @Test fun homeRendersActiveCountdownAndEmergencyEntry() {
        prepare(active = true)
        compose.setContent { SayBeerTheme { SayBeerApp(repo) } }
        compose.onNodeWithText("Đang khóa 1 app").assertExists()
        compose.onNodeWithText("SOS  Khẩn cấp").performScrollTo().performClick()
        compose.onNodeWithText("Cần mở ngay?").assertIsDisplayed()
    }
    @Test fun idleHomeAndPickerPreserveScheduleDraft() {
        prepare()
        compose.setContent { SayBeerTheme { SayBeerApp(repo) } }
        compose.onNodeWithText("Hẹn lịch").performScrollTo().performClick()
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText("T6").performClick()
        compose.onNodeWithText("Chọn app cần khóa", substring = true).performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Chrome").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Lưu (${repo.current.selected.size})").performClick()
        compose.onNodeWithText("T6").assertIsSelected()
    }
    @Test fun workCommitmentAndWaitingDeadlineSurviveRecreation() {
        prepare(active = true)
        val restoration = StateRestorationTester(compose)
        restoration.setContent { SayBeerTheme { WorkScreen(repo, repo.current, now) {} } }
        compose.onNodeWithText("Gõ lại câu trên…").performTextInput("chỉ nhắn việc, không tâm sự")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Gõ lại câu trên…").assertTextContains("chỉ nhắn việc, không tâm sự")
        compose.onNodeWithText("Hít thở đã…", substring = true).assertIsNotEnabled()
        assertEquals(0, repo.current.session!!.windowsUsed)
    }
}
