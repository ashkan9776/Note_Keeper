package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Soft-deletes notes by moving them to the Trash. Clears the pin so a restored note does not
 * reappear pinned, and stamps [Note.trashedAt] to drive the retention auto-purge.
 */
class MoveToTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        val now = LocalDateTime.now()
        repository.upsertNote(
            note.copy(isTrashed = true, trashedAt = now, isPinned = false, updatedAt = now)
        )
    }

    suspend operator fun invoke(notes: List<Note>) {
        notes.forEach { invoke(it) }
    }
}
