package com.ahoura.notekeeper.presentation.editor

import com.ahoura.notekeeper.domain.model.ChecklistItem
import com.ahoura.notekeeper.domain.model.NoteColor
import java.time.LocalDateTime

/** Editable snapshot of the note currently open in the editor. */
data class EditorUiState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: NoteColor = NoteColor.DEFAULT,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isNewNote: Boolean = true,
    val labels: List<String> = emptyList(),
    val reminderAt: LocalDateTime? = null,
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList()
) {
    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank() && checklistItems.none { it.text.isNotBlank() }
}

/** One-shot effects raised by the editor when the user leaves the screen. */
sealed interface EditorEvent {
    data object Saved : EditorEvent
    data object DiscardedEmpty : EditorEvent
    data object Deleted : EditorEvent
    data object Closed : EditorEvent
}
