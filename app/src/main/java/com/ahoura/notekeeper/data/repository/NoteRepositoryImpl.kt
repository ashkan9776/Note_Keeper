package com.ahoura.notekeeper.data.repository

import com.ahoura.notekeeper.data.local.NoteDao
import com.ahoura.notekeeper.data.reminder.ReminderScheduler
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Room-backed implementation of [NoteRepository]. Maps entities to domain models on the way out
 * and back on the way in; all suspend calls run on Room's own executor. Persisting a note also
 * (re)arms or cancels its reminder alarm via [ReminderScheduler], keeping that side effect in one
 * place so every code path that saves a note stays consistent.
 */
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val reminderScheduler: ReminderScheduler
) : NoteRepository {

    override fun getActiveNotes(): Flow<List<Note>> =
        dao.getAllActiveNotes().map { entities -> entities.map { it.toDomain() } }

    override fun getArchivedNotes(): Flow<List<Note>> =
        dao.getArchivedNotes().map { entities -> entities.map { it.toDomain() } }

    override fun getTrashedNotes(): Flow<List<Note>> =
        dao.getTrashedNotes().map { entities -> entities.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        dao.searchNotes(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): Note? = dao.getNoteById(id)?.toDomain()

    override suspend fun getNotesWithReminders(): List<Note> =
        dao.getNotesWithReminders().map { it.toDomain() }

    override suspend fun upsertNote(note: Note): Long {
        val id = dao.insertNote(note.toEntity())
        val saved = note.copy(id = id)
        // A reminder is only live while the note is visible and its time is still in the future;
        // re-arming a past reminder (e.g. on autosave) would fire it again immediately.
        val live = saved.reminderAt?.isAfter(LocalDateTime.now()) == true &&
            !saved.isArchived && !saved.isTrashed
        if (live) reminderScheduler.schedule(saved) else reminderScheduler.cancel(id)
        return id
    }

    override suspend fun deleteNote(note: Note) {
        reminderScheduler.cancel(note.id)
        dao.deleteNote(note.toEntity())
    }

    override suspend fun deleteNotesByIds(ids: List<Long>) {
        ids.forEach { reminderScheduler.cancel(it) }
        dao.deleteNotesByIds(ids)
    }

    override suspend fun emptyTrash() = dao.emptyTrash()

    override suspend fun purgeExpiredTrash(cutoffMillis: Long) = dao.purgeExpiredTrash(cutoffMillis)
}
