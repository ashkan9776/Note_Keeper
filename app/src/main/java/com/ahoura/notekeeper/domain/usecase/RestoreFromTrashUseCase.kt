package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/** Restores a note out of the Trash back to wherever it lived before (home or archive). */
class RestoreFromTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.upsertNote(
            note.copy(isTrashed = false, trashedAt = null, updatedAt = LocalDateTime.now())
        )
    }

    suspend operator fun invoke(notes: List<Note>) {
        notes.forEach { invoke(it) }
    }
}
