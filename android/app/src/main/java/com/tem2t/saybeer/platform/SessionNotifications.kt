package com.tem2t.saybeer.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tem2t.saybeer.MainActivity
import com.tem2t.saybeer.R
import com.tem2t.saybeer.data.BeerState
import java.text.DateFormat
import java.util.Date

object SessionNotifications {
    private const val CHANNEL = "session"
    fun createChannel(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Phiên SayBeer", NotificationManager.IMPORTANCE_LOW))
    }
    fun update(context: Context, state: BeerState) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val s = state.session
        if (s == null) { manager.cancel(42); return }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val window = s.windowEnd > System.currentTimeMillis()
        val end = if (window) s.windowEnd else s.end
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(end))
        val pending = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        manager.notify(42, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_mug)
            .setContentTitle(if (window) "Cửa sổ nhắn việc đang mở" else "SayBeer đang bảo vệ bạn")
            .setContentText(if (window) "Tự khóa lại lúc $time" else "${state.selected.size} app · Mở khóa lúc $time")
            .setContentIntent(pending).setOngoing(true).setOnlyAlertOnce(true).setSilent(true).build())
    }
}
