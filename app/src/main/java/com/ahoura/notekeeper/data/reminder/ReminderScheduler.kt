package com.ahoura.notekeeper.data.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ahoura.notekeeper.domain.model.Note
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels exact alarms that drive note reminders. Lives in the data layer because it
 * is the side effect of persisting a note: [com.ahoura.notekeeper.data.repository.NoteRepositoryImpl]
 * calls [schedule]/[cancel] whenever a note is upserted or removed.
 *
 * The alarm's [PendingIntent] request code is the note id, so re-scheduling the same note replaces
 * its previous alarm and [cancel] can target it precisely.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** (Re)schedules the alarm for [note]; no-ops if the note has no future reminder time. */
    @SuppressLint("MissingPermission")
    fun schedule(note: Note) {
        val reminder = note.reminderAt ?: return
        val triggerAt = reminder.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val pendingIntent = broadcastPendingIntent(
            note = note,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            // Lost exact-alarm permission between the check and the call — fall back to inexact.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    /** Cancels any pending alarm for the given note id. */
    fun cancel(noteId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun broadcastPendingIntent(note: Note, flags: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderNotifications.EXTRA_NOTE_ID, note.id)
            putExtra(ReminderNotifications.EXTRA_TITLE, note.title)
            putExtra(ReminderNotifications.EXTRA_BODY, note.reminderBody())
        }
        return PendingIntent.getBroadcast(context, note.id.toInt(), intent, flags)
    }

    private fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
}

/** A short notification body for the reminder, derived from content or checklist items. */
internal fun Note.reminderBody(): String = when {
    content.isNotBlank() -> content
    checklistItems.isNotEmpty() -> checklistItems.joinToString(", ") { it.text }
    else -> ""
}
