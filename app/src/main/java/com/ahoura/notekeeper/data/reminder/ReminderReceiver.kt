package com.ahoura.notekeeper.data.reminder

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahoura.notekeeper.MainActivity
import com.ahoura.notekeeper.R

/**
 * Fired by [ReminderScheduler]'s alarm. Posts a notification for the note; tapping it deep-links
 * back into the editor via [MainActivity]. The notification id is the note id so a note never
 * stacks duplicate reminders.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(ReminderNotifications.EXTRA_NOTE_ID, -1L)
        if (noteId == -1L) return

        val title = intent.getStringExtra(ReminderNotifications.EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(ReminderNotifications.EXTRA_BODY).orEmpty()

        // Respect the runtime notification permission on Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderNotifications.EXTRA_OPEN_NOTE_ID, noteId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderNotifications.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title.ifBlank { context.getString(R.string.reminder_notification_fallback_title) })
            .apply { if (body.isNotBlank()) setContentText(body).setStyle(NotificationCompat.BigTextStyle().bigText(body)) }
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(noteId.toInt(), notification)
    }
}
