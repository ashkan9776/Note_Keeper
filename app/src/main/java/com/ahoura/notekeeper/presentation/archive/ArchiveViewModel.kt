package com.ahoura.notekeeper.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.repository.NoteRepository
import com.ahoura.notekeeper.domain.usecase.ToggleArchiveNoteUseCase
import com.ahoura.notekeeper.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    repository: NoteRepository,
    private val toggleArchive: ToggleArchiveNoteUseCase
) : ViewModel() {

    val uiState: StateFlow<UiState<List<Note>>> = repository.getArchivedNotes()
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unable to load archive")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Moves a note back to Home. */
    fun unarchive(note: Note) = viewModelScope.launch {
        toggleArchive(note)
    }
}
