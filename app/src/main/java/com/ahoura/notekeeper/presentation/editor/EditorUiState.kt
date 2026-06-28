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
    val checklistItems: List<ChecklistItem> = emptyList(),
    /** When the note was first created; null until it has been persisted once. */
    val createdAt: LocalDateTime? = null
) {
    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank() && checklistItems.none { it.text.isNotBlank() }

    /** The note body as plain text, with no checkbox markers — used for word/character stats. */
    private val plainBody: String
        get() = if (isChecklist) {
            checklistItems.joinToString(" ") { it.text }
        } else {
            content
        }

    /** Live word count across the title and body; 0 for an empty note. */
    val wordCount: Int
        get() = "$title $plainBody".split(WHITESPACE).count { it.isNotBlank() }

    /** Live character count across the title and body (excluding the separating space). */
    val characterCount: Int
        get() = title.length + plainBody.length

    /** Estimated reading time in whole minutes, rounded up (min 1 when there is any text). */
    val readingMinutes: Int
        get() = if (wordCount == 0) 0 else ((wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE)

    /**
     * The note rendered as shareable plain text: title, then the body. Checklist items keep their
     * checked/unchecked markers so the recipient sees the to-do state.
     */
    val shareText: String
        get() {
            val body = if (isChecklist) {
                checklistItems
                    .filter { it.text.isNotBlank() }
                    .joinToString("\n") { (if (it.isChecked) "[x] " else "[ ] ") + it.text }
            } else {
                content
            }
            return listOf(title, body).filter { it.isNotBlank() }.joinToString("\n\n")
        }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val WORDS_PER_MINUTE = 200
    }
}

/** One-shot effects raised by the editor when the user leaves the screen. */
sealed interface EditorEvent {
    data object Saved : EditorEvent
    data object DiscardedEmpty : EditorEvent
    data object Deleted : EditorEvent
    data object Closed : EditorEvent
}
