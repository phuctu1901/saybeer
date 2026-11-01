package com.tem2t.saybeer.data

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class SessionEngineTest {
    private val now = Instant.parse("2026-10-23T11:00:00Z").toEpochMilli() // Friday 18:00 in Saigon
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private fun active() = SessionEngine.start(BeerState(selected = setOf("chat.app")), now, now + 240 * MINUTE)

    @Test fun emptySelectionCannotStart() {
        assertNull(SessionEngine.start(BeerState(), now, now + 240 * MINUTE).session)
    }
    @Test fun sessionBlocksOnlySelectedApps() {
        assertTrue(SessionEngine.shouldBlock(active(), "chat.app", now))
        assertFalse(SessionEngine.shouldBlock(active(), "phone", now))
        assertFalse(SessionEngine.shouldBlock(active(), "chat.app", now + 240 * MINUTE))
    }
    @Test fun workWindowReblocksAtDeadlineAndCannotOverlap() {
        val window = SessionEngine.openWindow(active(), now)
        assertFalse(SessionEngine.shouldBlock(window, "chat.app", now + MINUTE))
        assertTrue(SessionEngine.shouldBlock(window, "chat.app", now + 15 * MINUTE))
        assertEquals(window, SessionEngine.openWindow(window, now + MINUTE))
        assertEquals(0L, SessionEngine.tick(window, now + 15 * MINUTE, zone).session!!.windowEnd)
    }
    @Test fun threeWindowLimitAndClampingToSessionEnd() {
        var s = active()
        repeat(3) { i -> s = SessionEngine.openWindow(s, now + i * 20 * MINUTE) }
        assertEquals(3, s.session!!.windowsUsed)
        assertEquals(s, SessionEngine.openWindow(s, now + 100 * MINUTE))
        val nearEnd = SessionEngine.openWindow(active(), now + 239 * MINUTE)
        assertEquals(nearEnd.session!!.end, nearEnd.session!!.windowEnd)
    }
    @Test fun threeFailuresLockForFiveMinutesWithoutExtendingWhileLocked() {
        var s = active()
        repeat(3) { s = SessionEngine.fail(s, now) }
        assertEquals(now + 5 * MINUTE, s.session!!.lockoutUntil)
        assertEquals(s, SessionEngine.fail(s, now + MINUTE))
        assertEquals(0L, SessionEngine.tick(s, now + 5 * MINUTE, zone).session!!.lockoutUntil)
    }
    @Test fun autoEndRecordsPlannedEndEvenWhenAppWasClosed() {
        val s = SessionEngine.tick(active(), now + 999 * MINUTE, zone)
        assertNull(s.session)
        assertEquals(1, s.history.size)
        assertEquals(now + 240 * MINUTE, s.history.single().actualEnd)
        assertTrue(s.history.single().survived)
        assertEquals(s, SessionEngine.tick(s, now + 1000 * MINUTE, zone))
    }
    @Test fun earlyFinishReasonAndHistoryCap() {
        var state = BeerState(selected = setOf("chat.app"))
        repeat(205) { i ->
            state = SessionEngine.start(state, now + i * 500 * MINUTE, now + (i * 500 + 240) * MINUTE)
            state = SessionEngine.finish(state, now + (i * 500 + 10) * MINUTE, if (i % 2 == 0) "test" else "emergency")
        }
        assertEquals(200, state.history.size)
        assertEquals(0, state.survived)
        assertEquals(0, state.streak)
    }
    @Test fun scheduleCrossesMidnightAndRunsOnceAfterEarlyUnlock() {
        val cfg = WeeklySchedule(true, setOf(5), 18 * 60, 720)
        var s = SessionEngine.tick(BeerState(selected = setOf("chat.app"), schedule = cfg), now + 600 * MINUTE, zone)
        assertEquals(now, s.session!!.start)
        assertEquals(now + 720 * MINUTE, s.session!!.end)
        s = SessionEngine.finish(s, now + 601 * MINUTE, "test")
        assertNull(SessionEngine.tick(s, now + 602 * MINUTE, zone).session)
    }
    @Test fun scheduleDoesNotRestartAfterRebootOrExtendExistingSession() {
        val cfg = WeeklySchedule(true, setOf(5), 18 * 60, 720)
        val s = active().copy(schedule = cfg)
        val ticked = SessionEngine.tick(s, now + MINUTE, zone)
        assertEquals(s.session, ticked.session)
        val ended = SessionEngine.tick(ticked, now + 241 * MINUTE, zone)
        assertNull(ended.session)
        assertNull(SessionEngine.tick(ended, now + 242 * MINUTE, zone).session)
    }
    @Test fun nextScheduleUsesIsoWeekdaysAndSkipsPastOccurrence() {
        val cfg = WeeklySchedule(true, setOf(5), 1080, 720)
        assertEquals(now + 7 * 24 * 60 * MINUTE, cfg.next(now, zone))
        assertNull(cfg.copy(enabled = false).next(now, zone))
        assertNull(cfg.copy(days = emptySet()).next(now, zone))
    }
    @Test fun scheduleHandlesDstSpringGap() {
        val ny = ZoneId.of("America/New_York")
        val before = Instant.parse("2026-03-08T05:00:00Z").toEpochMilli()
        val cfg = WeeklySchedule(true, setOf(7), 150, 60)
        assertEquals(Instant.parse("2026-03-08T07:30:00Z").toEpochMilli(), cfg.next(before, ny))
    }
    @Test fun normalizationRetainsDiacriticsAndAllowsCanonicalUnicode() {
        assertEquals(normalize("nồi đồng"), normalize("  NỒI   ĐỒNG  "))
        assertNotEquals(normalize("noi dong"), normalize("nồi đồng"))
        assertEquals(normalize("chỉ nhắn việc, không tâm sự", true), normalize("chỉ nhắn việc không tâm sự", true))
    }
    @Test fun statisticsOnlyCountConsecutiveSafeSessions() {
        val record = Record(now, now + MINUTE, now + MINUTE, "done", 0)
        val s = BeerState(history = listOf(record, record.copy(reason = "test"), record, record))
        assertEquals(3, s.survived)
        assertEquals(2, s.streak)
    }
}
