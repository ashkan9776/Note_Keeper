package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Flips a note's archived flag and persists it. Archiving also clears the pin so the note
 * does not reappear pinned when restored.
 */
class ToggleArchiveNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long {
        val archiving = !note.isArchived
        return repository.upsertNote(
            note.copy(
                isArchived = archiving,
                isPinned = if (archiving) false else note.isPinned,
                updatedAt = LocalDateTime.now()
            )
        )
    }
}
