package com.tem2t.saybeer.platform

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tem2t.saybeer.data.BeerState
import com.tem2t.saybeer.repository

object SessionAlarms {
    fun exactAllowed(context: Context) = Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    fun reschedule(context: Context, state: BeerState) {
        val now = System.currentTimeMillis()
        val alarm = context.getSystemService(AlarmManager::class.java)
        val candidates = listOfNotNull(state.session?.end, state.session?.windowEnd?.takeIf { it > now }, state.schedule.next(now))
        val pending = PendingIntent.getBroadcast(context, 1, Intent(context, SessionReceiver::class.java).setAction("com.tem2t.saybeer.TICK"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)
        val next = candidates.filter { it > now }.minOrNull() ?: return
        try {
            if (exactAllowed(context)) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
            else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
        } catch (_: SecurityException) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending)
        }
    }
}

class SessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf("com.tem2t.saybeer.TICK", Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) return
        context.repository.tick()
        SessionAlarms.reschedule(context, context.repository.current)
        SessionNotifications.update(context, context.repository.current)
    }
}
