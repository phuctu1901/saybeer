package com.tem2t.saybeer.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** One application-scoped writer, shared by activity, alarms, and accessibility service. */
class SessionRepository(context: Context) {
    private val prefs = context.getSharedPreferences("saybeer", Context.MODE_PRIVATE)
    private val mutable = MutableStateFlow(decode(prefs.getString("state", null)))
    val state = mutable.asStateFlow()
    val current get() = mutable.value
    var onChanged: (() -> Unit)? = null

    @Synchronized fun update(transform: (BeerState) -> BeerState) {
        val next = transform(current)
        if (next == current) return
        // Commit before publishing: an interrupted process must not lose a lock or a used window.
        check(prefs.edit().putString("state", encode(next).toString()).commit()) { "Không thể lưu trạng thái phiên" }
        mutable.value = next
        onChanged?.invoke()
    }
    fun tick(now: Long = System.currentTimeMillis()) = update { SessionEngine.tick(it, now) }
    fun start(end: Long) = update { SessionEngine.start(it, System.currentTimeMillis(), end) }
    fun finish(reason: String) = update { SessionEngine.finish(it, System.currentTimeMillis(), reason) }
    fun openWindow() = update { SessionEngine.openWindow(it, System.currentTimeMillis()) }
    fun fail() = update { SessionEngine.fail(it, System.currentTimeMillis()) }

    private fun encode(s: BeerState) = JSONObject().apply {
        put("version", 1); put("onboarded", s.onboarded); put("selected", JSONArray(s.selected.toList())); put("math", s.math)
        put("lastScheduledStart", s.lastScheduledStart)
        put("schedule", JSONObject().apply {
            put("enabled", s.schedule.enabled); put("days", JSONArray(s.schedule.days.toList()))
            put("startMinute", s.schedule.startMinute); put("duration", s.schedule.durationMinutes)
        })
        s.session?.let { session -> put("session", JSONObject().apply {
            put("start", session.start); put("end", session.end); put("windowEnd", session.windowEnd)
            put("windows", session.windowsUsed); put("failures", session.failures)
            put("lockout", session.lockoutUntil); put("math", session.math)
        }) }
        put("history", JSONArray().apply { s.history.forEach { r -> put(JSONObject().apply {
            put("start", r.start); put("end", r.plannedEnd); put("actualEnd", r.actualEnd)
            put("reason", r.reason); put("windows", r.windows)
        }) } })
    }
    private fun decode(raw: String?): BeerState {
        if (raw == null) return BeerState()
        return runCatching {
            val j = JSONObject(raw)
            val cfg = j.optJSONObject("schedule") ?: JSONObject()
            val a = j.optJSONArray("selected") ?: JSONArray()
            val days = cfg.optJSONArray("days") ?: JSONArray()
            val h = j.optJSONArray("history") ?: JSONArray()
            BeerState(j.optBoolean("onboarded"), (0 until a.length()).map { a.getString(it) }.toSet(), j.optBoolean("math", true),
                j.optJSONObject("session")?.let { Session(it.getLong("start"), it.getLong("end"), it.optLong("windowEnd"),
                    it.optInt("windows"), it.optInt("failures"), it.optLong("lockout"), it.optBoolean("math", true)) },
                (0 until h.length()).map { h.getJSONObject(it).let { r -> Record(r.getLong("start"), r.getLong("end"),
                    r.getLong("actualEnd"), r.getString("reason"), r.optInt("windows")) } }.takeLast(200),
                WeeklySchedule(cfg.optBoolean("enabled"), (0 until days.length()).map { days.getInt(it) }.toSet(),
                    cfg.optInt("startMinute", 1080), cfg.optInt("duration", 720)), j.optLong("lastScheduledStart"))
        }.getOrElse { throw IllegalStateException("Không thể đọc dữ liệu SayBeer; dữ liệu gốc vẫn được giữ.", it) }
    }
}
