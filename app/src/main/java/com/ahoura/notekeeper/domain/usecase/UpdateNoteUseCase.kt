package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import javax.inject.Inject

/** Persists changes to an existing note, refreshing its updated timestamp. */
class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long =
        repository.upsertNote(note.copy(updatedAt = LocalDateTime.now()))
}
