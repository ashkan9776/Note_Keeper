package com.ahoura.notekeeper.domain.model

/**
 * In-app language choice. [SYSTEM] follows the device locale list; the others force a specific
 * per-app locale (and, for [PERSIAN], a right-to-left layout direction).
 *
 * [tag] is the BCP-47 language tag handed to `AppCompatDelegate.setApplicationLocales`; an empty
 * tag means "clear the override and follow the system".
 */
enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ENGLISH("en"),
    PERSIAN("fa");

    companion object {
        /** Resolves the active language from an applied locale tag (e.g. from AppCompatDelegate). */
        fun fromTag(tag: String?): AppLanguage = when {
            tag.isNullOrBlank() -> SYSTEM
            tag.startsWith("fa") -> PERSIAN
            tag.startsWith("en") -> ENGLISH
            else -> SYSTEM
        }
    }
}
