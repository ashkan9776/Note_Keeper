package com.ahoura.notekeeper.presentation.home

import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.NoteLayoutMode

/**
 * State for the Home screen: the async note list plus transient UI concerns (layout mode and the
 * multi-select set).
 */
data class HomeUiState(
    val notes: UiState<List<Note>> = UiState.Loading,
    val layoutMode: NoteLayoutMode = NoteLayoutMode.GRID,
    val selectedIds: Set<Long> = emptySet(),
    /** Every label in use, for the filter row. */
    val allLabels: List<String> = emptyList(),
    /** Active label filter, or null when showing all notes. */
    val selectedLabel: String? = null
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
    val selectionCount: Int get() = selectedIds.size

    /** Pinned notes from the loaded data, or empty while loading/errored. */
    val pinned: List<Note>
        get() = (notes as? UiState.Success)?.data?.filter { it.isPinned } ?: emptyList()

    val others: List<Note>
        get() = (notes as? UiState.Success)?.data?.filterNot { it.isPinned } ?: emptyList()
}

/** One-shot effects surfaced to the UI (snackbars). */
sealed interface HomeEvent {
    data class NotesDeleted(val count: Int) : HomeEvent
}
