package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import javax.inject.Inject

/** Permanently removes one or many notes. */
class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) = repository.deleteNote(note)

    suspend operator fun invoke(ids: List<Long>) = repository.deleteNotesByIds(ids)
}
