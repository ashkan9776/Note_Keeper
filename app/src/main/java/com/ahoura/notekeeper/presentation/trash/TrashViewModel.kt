package com.ahoura.notekeeper.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.usecase.DeleteNoteUseCase
import com.ahoura.notekeeper.domain.usecase.EmptyTrashUseCase
import com.ahoura.notekeeper.domain.usecase.GetTrashedNotesUseCase
import com.ahoura.notekeeper.domain.usecase.PurgeExpiredTrashUseCase
import com.ahoura.notekeeper.domain.usecase.RestoreFromTrashUseCase
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
class TrashViewModel @Inject constructor(
    getTrashedNotes: GetTrashedNotesUseCase,
    private val restoreFromTrash: RestoreFromTrashUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val emptyTrash: EmptyTrashUseCase,
    purgeExpiredTrash: PurgeExpiredTrashUseCase
) : ViewModel() {

    init {
        // Drop anything past the retention window as soon as the user opens the Trash.
        viewModelScope.launch { purgeExpiredTrash() }
    }

    val uiState: StateFlow<UiState<List<Note>>> = getTrashedNotes()
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unable to load trash")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun restore(note: Note) = viewModelScope.launch { restoreFromTrash(note) }

    fun deleteForever(note: Note) = viewModelScope.launch { deleteNote(note) }

    fun emptyTrashNow() = viewModelScope.launch { emptyTrash() }
}
