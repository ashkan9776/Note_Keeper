package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.repository.NoteRepository
import javax.inject.Inject

/** Permanently deletes every note in the Trash. */
class EmptyTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke() = repository.emptyTrash()
}
