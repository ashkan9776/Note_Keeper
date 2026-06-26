package com.ahoura.notekeeper.data.preferences

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.ahoura.notekeeper.domain.model.AppLanguage

/**
 * Thin wrapper over AppCompat's per-app locale APIs. Applying a language recreates the visible
 * activities so every `stringResource` and the layout direction (LTR/RTL) refresh immediately;
 * AppCompat also persists the choice (see the `AppLocalesMetadataHolderService` in the manifest,
 * which enables storage below Android 13).
 */
object LocaleManager {

    /** The language currently applied to the app. */
    fun current(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        val tag = if (locales.isEmpty) null else locales[0]?.language
        return AppLanguage.fromTag(tag)
    }

    /** Switches the app to [language]; [AppLanguage.SYSTEM] clears the override. */
    fun apply(language: AppLanguage) {
        val locales = if (language.tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
