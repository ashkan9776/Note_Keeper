package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/** Persists a brand-new note, stamping both created/updated timestamps to now. */
class CreateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long {
        val now = LocalDateTime.now()
        return repository.upsertNote(note.copy(createdAt = now, updatedAt = now))
    }
}
