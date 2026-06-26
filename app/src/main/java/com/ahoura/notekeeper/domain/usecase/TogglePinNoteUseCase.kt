package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/** Flips a note's pinned flag and persists it. */
class TogglePinNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long =
        repository.upsertNote(note.copy(isPinned = !note.isPinned, updatedAt = LocalDateTime.now()))
}
