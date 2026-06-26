package com.ahoura.notekeeper.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.data.preferences.SettingsDataStore
import com.ahoura.notekeeper.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns the persisted theme preference. The language choice is intentionally *not* held here — it
 * lives in AppCompat's locale store and is applied directly from the screen, since switching it
 * recreates the activity (and therefore this ViewModel).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsDataStore
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }
}
