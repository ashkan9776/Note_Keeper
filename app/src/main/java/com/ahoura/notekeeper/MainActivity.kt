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
import com.ahoura.notekeeper.data.security.BiometricAuthManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: SettingsDataStore

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    private var isUnlocked by mutableStateOf(false)

    // Note id to open when launched from a reminder notification
    private var deepLinkNoteId by mutableStateOf<Long?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Blocking check for initial security state to avoid UI flicker if possible,
        // or just handle it in Compose.
        val biometricEnabled = runBlocking { settings.isBiometricEnabled.first() }
        isUnlocked = !biometricEnabled

        deepLinkNoteId = intent.readReminderNoteId()
        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()
        
        setContent {
            val biometricEnabledState by settings.isBiometricEnabled.collectAsState(initial = biometricEnabled)
            
            LaunchedEffect(biometricEnabledState) {
                if (biometricEnabledState && !isUnlocked) {
                    authenticate()
                }
            }

            if (isUnlocked) {
                NoteKeeperRoot(
                    settings = settings,
                    deepLinkNoteId = deepLinkNoteId
                )
            } else {
                // Showing a blank surface or splash while locked
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {}
            }
        }
    }

    private fun authenticate() {
        if (biometricAuthManager.isBiometricAvailable(this)) {
            biometricAuthManager.authenticate(
                activity = this,
                title = getString(R.string.app_name),
                subtitle = getString(R.string.biometric_prompt_subtitle),
                onSuccess = { isUnlocked = true },
                onError = { /* Handle error or stay locked */ }
            )
        } else {
            // Biometrics not available but enabled? Should not happen if settings check is correct,
            // but fallback to unlocked to avoid lock-out.
            isUnlocked = true
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
