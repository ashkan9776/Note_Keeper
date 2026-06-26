package com.ahoura.notekeeper.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahoura.notekeeper.domain.repository.NoteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-arms every note reminder after a device reboot, since AlarmManager alarms do not survive
 * a restart. Reads persisted notes off the main thread via [goAsync].
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: NoteRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.getNotesWithReminders().forEach { scheduler.schedule(it) }
            } finally {
                pending.finish()
            }
        }
    }
}
