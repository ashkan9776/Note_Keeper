package com.ahoura.notekeeper.domain.usecase

import com.ahoura.notekeeper.domain.repository.NoteRepository
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Permanently deletes trashed notes older than [TRASH_RETENTION_DAYS], mirroring Google Keep's
 * auto-empty behavior. Invoked when the Trash screen opens.
 */
class PurgeExpiredTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke() {
        val cutoff = LocalDateTime.now().minusDays(TRASH_RETENTION_DAYS)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repository.purgeExpiredTrash(cutoff)
    }

    companion object {
        const val TRASH_RETENTION_DAYS = 7L
    }
}
