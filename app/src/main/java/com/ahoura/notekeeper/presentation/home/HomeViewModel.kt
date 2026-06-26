package com.ahoura.notekeeper.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.model.NoteColor
import com.ahoura.notekeeper.domain.usecase.GetAllNotesUseCase
import com.ahoura.notekeeper.domain.usecase.MoveToTrashUseCase
import com.ahoura.notekeeper.domain.usecase.RestoreFromTrashUseCase
import com.ahoura.notekeeper.domain.usecase.ToggleArchiveNoteUseCase
import com.ahoura.notekeeper.domain.usecase.TogglePinNoteUseCase
import com.ahoura.notekeeper.domain.usecase.UpdateNoteUseCase
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.NoteLayoutMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllNotes: GetAllNotesUseCase,
    private val togglePin: TogglePinNoteUseCase,
    private val toggleArchive: ToggleArchiveNoteUseCase,
    private val moveToTrash: MoveToTrashUseCase,
    private val restoreFromTrash: RestoreFromTrashUseCase,
    private val updateNote: UpdateNoteUseCase
) : ViewModel() {

    // UI-only state owned by the ViewModel; merged with the notes stream below.
    private val layoutMode = MutableStateFlow(NoteLayoutMode.GRID)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectedLabel = MutableStateFlow<String?>(null)

    private val notesState: StateFlow<UiState<List<Note>>> = getAllNotes()
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Unable to load notes")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val uiState: StateFlow<HomeUiState> =
        combine(notesState, layoutMode, selectedIds, selectedLabel) { notes, layout, selection, label ->
            // Label list is derived from the full, unfiltered set so chips never disappear mid-filter.
            val allLabels = (notes as? UiState.Success)?.data
                ?.flatMap { it.labels }?.distinct()?.sorted().orEmpty()
            val filtered = if (label != null && notes is UiState.Success) {
                UiState.Success(notes.data.filter { label in it.labels })
            } else {
                notes
            }
            HomeUiState(
                notes = filtered,
                layoutMode = layout,
                selectedIds = selection,
                allLabels = allLabels,
                selectedLabel = label
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Notes moved to Trash by the most recent delete, retained so the action can be undone. */
    private var recentlyTrashed: List<Note> = emptyList()

    fun toggleLayout() {
        layoutMode.update { if (it == NoteLayoutMode.GRID) NoteLayoutMode.LIST else NoteLayoutMode.GRID }
    }

    /** Filters the grid to [label], or clears the filter when null or the active label is re-tapped. */
    fun selectLabel(label: String?) {
        selectedLabel.update { current -> if (label == current) null else label }
    }

    fun onNoteLongPress(noteId: Long) {
        selectedIds.update { it + noteId }
    }

    fun toggleSelection(noteId: Long) {
        selectedIds.update { current ->
            if (noteId in current) current - noteId else current + noteId
        }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    private fun selectedNotes(): List<Note> {
        val data = (notesState.value as? UiState.Success)?.data ?: return emptyList()
        val ids = selectedIds.value
        return data.filter { it.id in ids }
    }

    fun pinSelected() = viewModelScope.launch {
        // If every selection is already pinned, toggling unpins them; otherwise pin all.
        val notes = selectedNotes()
        val shouldPin = notes.any { !it.isPinned }
        notes.filter { it.isPinned != shouldPin }.forEach { togglePin(it) }
        clearSelection()
    }

    fun archiveSelected() = viewModelScope.launch {
        selectedNotes().forEach { toggleArchive(it) }
        clearSelection()
    }

    fun changeColorForSelected(color: NoteColor) = viewModelScope.launch {
        selectedNotes().forEach { updateNote(it.copy(color = color)) }
        clearSelection()
    }

    fun deleteSelected() = viewModelScope.launch {
        val toTrash = selectedNotes()
        if (toTrash.isEmpty()) return@launch
        recentlyTrashed = toTrash
        moveToTrash(toTrash)
        clearSelection()
        _events.send(HomeEvent.NotesDeleted(toTrash.size))
    }

    /** Swipe-to-archive for a single note (list mode). */
    fun archiveNote(note: Note) = viewModelScope.launch {
        toggleArchive(note)
    }

    /** Restores the notes from the most recent delete back out of the Trash. */
    fun undoDelete() = viewModelScope.launch {
        restoreFromTrash(recentlyTrashed)
        recentlyTrashed = emptyList()
    }
}
