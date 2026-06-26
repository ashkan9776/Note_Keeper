package com.ahoura.notekeeper.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ahoura.notekeeper.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Persists lightweight UI preferences. Only the theme mode lives here — the language choice is
 * owned by `AppCompatDelegate.getApplicationLocales()` so the system can restore it across process
 * death and reflect changes made from Android's per-app language settings.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[THEME_MODE_KEY])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[THEME_MODE_KEY] = mode.name }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
