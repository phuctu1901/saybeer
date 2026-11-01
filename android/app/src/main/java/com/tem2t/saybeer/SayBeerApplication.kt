package com.tem2t.saybeer

import android.app.Application
import android.content.Context
import com.tem2t.saybeer.data.SessionRepository
import com.tem2t.saybeer.platform.SessionAlarms
import com.tem2t.saybeer.platform.SessionNotifications

class SayBeerApplication : Application() {
    lateinit var repository: SessionRepository
        private set
    override fun onCreate() {
        super.onCreate()
        repository = SessionRepository(this)
        SessionNotifications.createChannel(this)
        repository.onChanged = {
            SessionAlarms.reschedule(this, repository.current)
            SessionNotifications.update(this, repository.current)
        }
        repository.tick()
        SessionAlarms.reschedule(this, repository.current)
    }
}
val Context.repository: SessionRepository get() = (applicationContext as SayBeerApplication).repository
