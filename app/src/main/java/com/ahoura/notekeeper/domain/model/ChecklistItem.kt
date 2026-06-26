package com.ahoura.notekeeper.domain.model

/**
 * A single checkable line within a checklist-mode [Note]. Checklist notes store these instead of
 * (or alongside) free-form [Note.content]; persistence encodes the list as a JSON array string.
 */
data class ChecklistItem(
    val text: String = "",
    val isChecked: Boolean = false
)
