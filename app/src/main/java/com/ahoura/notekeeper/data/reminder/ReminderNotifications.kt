package com.ahoura.notekeeper.data.reminder

/** Shared constants for the reminder notification channel and intent extras. */
object ReminderNotifications {
    const val CHANNEL_ID = "reminders"

    /** Extras carried on the alarm broadcast and the notification's content intent. */
    const val EXTRA_NOTE_ID = "extra_note_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"

    /** Extra read by MainActivity to deep-link straight into a note when a reminder is tapped. */
    const val EXTRA_OPEN_NOTE_ID = "open_note_id"
}
