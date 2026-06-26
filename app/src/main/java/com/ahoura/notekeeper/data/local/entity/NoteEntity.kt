package com.ahoura.notekeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence model. Differs from the domain [com.ahoura.notekeeper.domain.model.Note]
 * in that timestamps are epoch millis and labels/checklist items are stored as JSON array strings.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val labelsJson: String,
    /** Reminder fire time as epoch millis, or null when no reminder is set. */
    val reminderAt: Long? = null,
    val isChecklist: Boolean = false,
    /** Checklist items encoded as a JSON array; "[]" when the note is not a checklist. */
    val checklistJson: String = "[]",
    val isTrashed: Boolean = false,
    /** When the note was trashed, as epoch millis, or null when not trashed. */
    val trashedAt: Long? = null
)
