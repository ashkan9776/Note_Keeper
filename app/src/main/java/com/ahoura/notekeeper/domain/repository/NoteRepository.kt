package com.ahoura.notekeeper.domain.repository

import com.ahoura.notekeeper.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over note persistence. The domain layer depends only on this contract,
 * keeping Room (and any future data source) an implementation detail.
 */
interface NoteRepository {

    fun getActiveNotes(): Flow<List<Note>>

    fun getArchivedNotes(): Flow<List<Note>>

    /** Notes currently in the Trash, most recently trashed first. */
    fun getTrashedNotes(): Flow<List<Note>>

    fun searchNotes(query: String): Flow<List<Note>>

    suspend fun getNoteById(id: Long): Note?

    /** Every note carrying a reminder; used to re-arm alarms after a reboot. */
    suspend fun getNotesWithReminders(): List<Note>

    /** Inserts or replaces a note and returns its row id. */
    suspend fun upsertNote(note: Note): Long

    suspend fun deleteNote(note: Note)

    suspend fun deleteNotesByIds(ids: List<Long>)

    /** Permanently removes every trashed note. */
    suspend fun emptyTrash()

    /** Permanently removes trashed notes older than [cutoffMillis] (epoch millis). */
    suspend fun purgeExpiredTrash(cutoffMillis: Long)
}
