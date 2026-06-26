package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** Streams active notes whose title or content match [query]; blank queries yield nothing. */
class SearchNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) flowOf(emptyList()) else repository.searchNotes(trimmed)
    }
}
