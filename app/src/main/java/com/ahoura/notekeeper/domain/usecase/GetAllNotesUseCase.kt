package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams all active (non-archived) notes, pinned first, most-recently-updated next. */
class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> = repository.getActiveNotes()
}
