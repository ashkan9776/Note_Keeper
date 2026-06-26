package com.ahoura.notekeeper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.ahoura.notekeeper.data.reminder.ReminderNotifications
import dagger.hilt.android.HiltAndroidApp

/** Application entry point that bootstraps Hilt's dependency graph and the reminder channel. */
@HiltAndroidApp
class NoteKeeperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }

    private fun createReminderChannel() {
        val channel = NotificationChannel(
            ReminderNotifications.CHANNEL_ID,
            getString(R.string.action_reminder),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.reminder_notification_fallback_title)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
