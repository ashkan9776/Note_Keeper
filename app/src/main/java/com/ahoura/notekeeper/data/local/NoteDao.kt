package com.ahoura.notekeeper.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ahoura.notekeeper.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isTrashed = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY trashedAt DESC")
    fun getTrashedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    /** All notes carrying a reminder, regardless of archived state but excluding trash (for boot reschedule). */
    @Query("SELECT * FROM notes WHERE reminderAt IS NOT NULL AND isTrashed = 0")
    suspend fun getNotesWithReminders(): List<NoteEntity>

    @Query(
        "SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%') AND isArchived = 0 AND isTrashed = 0 " +
            "ORDER BY isPinned DESC, updatedAt DESC"
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotesByIds(ids: List<Long>)

    /** Permanently removes every trashed note ("Empty trash"). */
    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun emptyTrash()

    /** Permanently removes trashed notes older than [cutoffMillis] (retention auto-purge). */
    @Query("DELETE FROM notes WHERE isTrashed = 1 AND trashedAt IS NOT NULL AND trashedAt < :cutoffMillis")
    suspend fun purgeExpiredTrash(cutoffMillis: Long)
}
