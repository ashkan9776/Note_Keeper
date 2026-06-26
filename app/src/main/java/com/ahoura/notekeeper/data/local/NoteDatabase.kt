package com.ahoura.notekeeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ahoura.notekeeper.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "notekeeper.db"
    }
}
