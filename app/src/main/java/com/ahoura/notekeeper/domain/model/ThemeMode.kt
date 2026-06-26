package com.ahoura.notekeeper.domain.model

/** User-selectable theme preference; [SYSTEM] defers to the device-wide light/dark setting. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        /** Parses a persisted name back to a [ThemeMode], falling back to [SYSTEM] when unknown. */
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
