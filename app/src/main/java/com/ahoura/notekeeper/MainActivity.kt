package com.ahoura.notekeeper

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.data.preferences.SettingsDataStore
import com.ahoura.notekeeper.data.reminder.ReminderNotifications
import com.ahoura.notekeeper.domain.model.ThemeMode
import com.ahoura.notekeeper.navigation.AppNavGraph
import com.ahoura.notekeeper.ui.theme.NoteKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: SettingsDataStore

    // Note id to open when launched from a reminder notification; backed by state so onNewIntent
    // (app already running) also routes to the editor.
    private var deepLinkNoteId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkNoteId = intent.readReminderNoteId()
        requestNotificationPermissionIfNeeded()
        // Edge-to-edge; status/navigation bar icon contrast adapts to the system theme.
        enableEdgeToEdge()
        setContent {
            NoteKeeperRoot(
                settings = settings,
                deepLinkNoteId = deepLinkNoteId
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkNoteId = intent.readReminderNoteId()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun Intent.readReminderNoteId(): Long? {
        val id = getLongExtra(ReminderNotifications.EXTRA_OPEN_NOTE_ID, -1L)
        return if (id != -1L) id else null
    }
}

@Composable
private fun NoteKeeperRoot(
    settings: SettingsDataStore,
    deepLinkNoteId: Long?
) {
    val themeMode by settings.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    NoteKeeperTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavGraph(deepLinkNoteId = deepLinkNoteId)
        }
    }
}
