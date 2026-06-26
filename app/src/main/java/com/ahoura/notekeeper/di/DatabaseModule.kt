package com.ahoura.notekeeper.di

import android.content.Context
import androidx.room.Room
import com.ahoura.notekeeper.data.local.NoteDao
import com.ahoura.notekeeper.data.local.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(context, NoteDatabase::class.java, NoteDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideNoteDao(database: NoteDatabase): NoteDao = database.noteDao()
}
