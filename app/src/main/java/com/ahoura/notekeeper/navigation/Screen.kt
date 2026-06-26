package com.ahoura.notekeeper.navigation

/** Destinations in the app, with helpers for building parameterized routes. */
sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object Editor : Screen("editor?noteId={noteId}") {
        const val ARG_NOTE_ID = "noteId"

        /** Sentinel id meaning "create a new note" rather than editing an existing one. */
        const val NEW_NOTE_ID = -1L

        fun createRoute(noteId: Long = NEW_NOTE_ID) = "editor?noteId=$noteId"
    }

    data object Search : Screen("search")

    data object Archive : Screen("archive")

    data object Trash : Screen("trash")

    data object Settings : Screen("settings")
}
