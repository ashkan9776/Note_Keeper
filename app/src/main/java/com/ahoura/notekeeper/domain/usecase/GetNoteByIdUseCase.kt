package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import javax.inject.Inject

/** Loads a single note by id, or null when it no longer exists. */
class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Long): Note? = repository.getNoteById(id)
}
