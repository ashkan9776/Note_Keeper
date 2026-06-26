package com.ahoura.notekeeper.domain.model

import java.time.LocalDateTime

/**
 * Core domain representation of a single note. This is the model the presentation
 * layer works with; persistence concerns (epoch millis, JSON labels) live in the data layer.
 */
data class Note(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: NoteColor = NoteColor.DEFAULT,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val labels: List<String> = emptyList(),
    /** When set, a notification fires at this time. Null means no reminder. */
    val reminderAt: LocalDateTime? = null,
    /** True when the note is a to-do list; [checklistItems] is then the body instead of [content]. */
    val isChecklist: Boolean = false,
    val checklistItems: List<ChecklistItem> = emptyList(),
    /** True while the note sits in the Trash awaiting restore or permanent deletion. */
    val isTrashed: Boolean = false,
    /** When the note was moved to Trash; used to auto-purge after the retention window. */
    val trashedAt: LocalDateTime? = null
) {
    /**
     * A note with no title, no content, and no non-blank checklist items is considered empty and is
     * discarded on save.
     */
    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank() && checklistItems.none { it.text.isNotBlank() }

    /** Number of checked items over total items — used for the card's progress label. */
    val checkedCount: Int get() = checklistItems.count { it.isChecked }
}

/**
 * The palette a note can be tinted with. [hexValue] is the light-theme surface color.
 * Dark-theme equivalents are resolved in the UI layer (see NoteColor.toComposeColor).
 */
enum class NoteColor(val hexValue: String) {
    DEFAULT("#FFFFFF"),
    RED("#FFCDD2"),
    PINK("#F8BBD9"),
    ORANGE("#FFE0B2"),
    YELLOW("#FFF9C4"),
    GREEN("#C8E6C9"),
    TEAL("#B2EBF2"),
    BLUE("#BBDEFB"),
    PURPLE("#E1BEE7"),
    GRAY("#F5F5F5"),
    CHARCOAL("#37474F");

    companion object {
        /** Resolves a persisted hex string back to a [NoteColor], defaulting to [DEFAULT]. */
        fun fromHex(hex: String): NoteColor =
            entries.firstOrNull { it.hexValue.equals(hex, ignoreCase = true) } ?: DEFAULT
    }
}
