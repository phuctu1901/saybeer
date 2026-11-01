package com.tem2t.saybeer.data

import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId

const val MINUTE = 60_000L
const val MAX_WINDOWS = 3
const val WINDOW_MINUTES = 15

data class Session(val start: Long, val end: Long, val windowEnd: Long = 0, val windowsUsed: Int = 0,
                   val failures: Int = 0, val lockoutUntil: Long = 0, val math: Boolean = true)
data class Record(val start: Long, val plannedEnd: Long, val actualEnd: Long, val reason: String, val windows: Int) {
    val survived get() = reason == "done" || reason == "auto"
    val duration get() = (actualEnd - start).coerceAtLeast(0)
}
data class WeeklySchedule(val enabled: Boolean = false, val days: Set<Int> = emptySet(),
                          val startMinute: Int = 18 * 60, val durationMinutes: Int = 12 * 60) {
    val valid get() = days.isNotEmpty() && days.all { it in 1..7 } && startMinute in 0..1439 && durationMinutes in 15..1439

    // ISO weekdays: Monday=1, Sunday=7. ZonedDateTime handles DST and midnight.
    fun occurrences(now: Long, zone: ZoneId = ZoneId.systemDefault()): List<Long> {
        if (!enabled || !valid) return emptyList()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        return (-1L..7L).map { today.plusDays(it) }.filter { it.dayOfWeek.value in days }
            .map { it.atTime(startMinute / 60, startMinute % 60).atZone(zone).toInstant().toEpochMilli() }.sorted()
    }
    fun next(now: Long, zone: ZoneId = ZoneId.systemDefault()) = occurrences(now, zone).firstOrNull { it > now }
}

data class BeerState(val onboarded: Boolean = false, val selected: Set<String> = emptySet(),
                     val math: Boolean = true, val session: Session? = null, val history: List<Record> = emptyList(),
                     val schedule: WeeklySchedule = WeeklySchedule(), val lastScheduledStart: Long = 0) {
    val survived get() = history.count { it.survived }
    val hours get() = history.sumOf { it.duration } / 3_600_000.0
    val streak get() = history.asReversed().takeWhile { it.survived }.size
}

object SessionEngine {
    fun start(state: BeerState, now: Long, end: Long): BeerState {
        if (state.session != null || state.selected.isEmpty() || end <= now + MINUTE) return state
        return state.copy(session = Session(now, end, math = state.math))
    }
    fun finish(state: BeerState, now: Long, reason: String): BeerState {
        val s = state.session ?: return state
        val record = Record(s.start, s.end, now.coerceIn(s.start, s.end), reason, s.windowsUsed)
        return state.copy(session = null, history = (state.history + record).takeLast(200))
    }
    fun tick(state: BeerState, now: Long, zone: ZoneId = ZoneId.systemDefault()): BeerState {
        var result = state
        val s = result.session
        if (s != null) {
            result = when {
                now >= s.end -> finish(result, s.end, "done")
                else -> result.copy(session = s.copy(
                    windowEnd = if (s.windowEnd <= now) 0 else s.windowEnd,
                    lockoutUntil = if (s.lockoutUntil <= now) 0 else s.lockoutUntil))
            }
        }
        val occurrence = result.schedule.occurrences(now, zone).lastOrNull { it <= now }
        if (occurrence != null && occurrence > result.lastScheduledStart) {
            result = result.copy(lastScheduledStart = occurrence)
            val end = occurrence + result.schedule.durationMinutes * MINUTE
            if (result.session == null && end > now && result.selected.isNotEmpty()) {
                result = result.copy(session = Session(occurrence, end, math = result.math))
            }
        }
        return result
    }
    fun openWindow(state: BeerState, now: Long): BeerState {
        val s = state.session ?: return state
        if (now >= s.end || s.windowsUsed >= MAX_WINDOWS || s.windowEnd > now) return state
        return state.copy(session = s.copy(windowEnd = minOf(s.end, now + WINDOW_MINUTES * MINUTE), windowsUsed = s.windowsUsed + 1))
    }
    fun fail(state: BeerState, now: Long): BeerState {
        val s = state.session ?: return state
        if (s.lockoutUntil > now) return state
        val count = s.failures + 1
        return state.copy(session = s.copy(failures = if (count >= 3) 0 else count,
            lockoutUntil = if (count >= 3) now + 5 * MINUTE else 0))
    }
    fun shouldBlock(state: BeerState, pkg: String, now: Long): Boolean {
        val s = state.session ?: return false
        return now < s.end && now >= s.windowEnd && pkg in state.selected
    }
}

fun normalize(value: String, commitment: Boolean = false): String = Normalizer.normalize(value, Normalizer.Form.NFC)
    .lowercase(java.util.Locale.ROOT).let { if (commitment) it.replace(",", "") else it }.trim().split(Regex("\\s+")).joinToString(" ")

fun remaining(end: Long, now: Long): String {
    val seconds = ((end - now + 999) / 1000).coerceAtLeast(0)
    return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, seconds % 3600 / 60, seconds % 60)
    else "%02d:%02d".format(seconds / 60, seconds % 60)
}
