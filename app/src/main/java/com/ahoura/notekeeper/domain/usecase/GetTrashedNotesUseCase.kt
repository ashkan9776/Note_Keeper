package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the notes currently in the Trash, most recently trashed first. */
class GetTrashedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getTrashedNotes()
}
