package com.ahoura.notekeeper.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.domain.model.ChecklistItem
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.model.NoteColor
import com.ahoura.notekeeper.domain.usecase.CreateNoteUseCase
import com.ahoura.notekeeper.domain.usecase.DeleteNoteUseCase
import com.ahoura.notekeeper.domain.usecase.GetAllNotesUseCase
import com.ahoura.notekeeper.domain.usecase.GetNoteByIdUseCase
import com.ahoura.notekeeper.domain.usecase.MoveToTrashUseCase
import com.ahoura.notekeeper.domain.usecase.ToggleArchiveNoteUseCase
import com.ahoura.notekeeper.domain.usecase.UpdateNoteUseCase
import com.ahoura.notekeeper.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

private const val AUTOSAVE_DEBOUNCE_MS = 800L

@OptIn(FlowPreview::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getAllNotes: GetAllNotesUseCase,
    private val getNoteById: GetNoteByIdUseCase,
    private val createNote: CreateNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val toggleArchive: ToggleArchiveNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val moveToTrash: MoveToTrashUseCase
) : ViewModel() {

    private val initialNoteId: Long =
        savedStateHandle.get<Long>(Screen.Editor.ARG_NOTE_ID) ?: Screen.Editor.NEW_NOTE_ID

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /** Every label already used across notes, offered as suggestions in the label sheet. */
    val allLabels: StateFlow<List<String>> = getAllNotes()
        .map { notes -> notes.flatMap { it.labels }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<EditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hasUserEdited = false
    private var createdAt: java.time.LocalDateTime? = null

    init {
        if (initialNoteId != Screen.Editor.NEW_NOTE_ID) {
            loadNote(initialNoteId)
        }
        // Debounced auto-save: persist 800ms after the user stops editing.
        viewModelScope.launch {
            _uiState
                .drop(1)
                .debounce(AUTOSAVE_DEBOUNCE_MS)
                .collect { if (hasUserEdited) persist(it) }
        }
    }

    private fun loadNote(id: Long) = viewModelScope.launch {
        val note = getNoteById(id) ?: return@launch
        createdAt = note.createdAt
        _uiState.value = EditorUiState(
            noteId = note.id,
            title = note.title,
            content = note.content,
            color = note.color,
            isPinned = note.isPinned,
            isArchived = note.isArchived,
            isNewNote = false,
            labels = note.labels,
            reminderAt = note.reminderAt,
            isChecklist = note.isChecklist,
            checklistItems = note.checklistItems
        )
    }

    fun onTitleChange(value: String) {
        hasUserEdited = true
        _uiState.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        hasUserEdited = true
        _uiState.update { it.copy(content = value) }
    }

    fun onColorChange(color: NoteColor) {
        hasUserEdited = true
        _uiState.update { it.copy(color = color) }
    }

    fun togglePin() {
        hasUserEdited = true
        _uiState.update { it.copy(isPinned = !it.isPinned) }
    }

    /** Adds a label to the open note, trimming and ignoring blanks/duplicates (case-insensitive). */
    fun addLabel(raw: String) {
        val label = raw.trim()
        if (label.isEmpty()) return
        _uiState.update { state ->
            if (state.labels.any { it.equals(label, ignoreCase = true) }) state
            else {
                hasUserEdited = true
                state.copy(labels = state.labels + label)
            }
        }
    }

    fun removeLabel(label: String) {
        _uiState.update { state ->
            if (label !in state.labels) state
            else {
                hasUserEdited = true
                state.copy(labels = state.labels - label)
            }
        }
    }

    /** Adds the label if absent, removes it if present — used by the label sheet's checkboxes. */
    fun toggleLabel(label: String) {
        if (_uiState.value.labels.any { it.equals(label, ignoreCase = true) }) removeLabel(label)
        else addLabel(label)
    }

    // --- Reminders ---

    /** Sets or clears the note's reminder time. Persistence (re)arms the alarm via the repository. */
    fun setReminder(at: LocalDateTime?) {
        hasUserEdited = true
        _uiState.update { it.copy(reminderAt = at) }
    }

    // --- Checklist ---

    /**
     * Switches the note between free-text and checklist modes, converting the body so nothing is
     * lost: enabling splits [content] lines into items; disabling joins items back into [content].
     */
    fun toggleChecklistMode() {
        hasUserEdited = true
        _uiState.update { state ->
            if (state.isChecklist) {
                val joined = state.checklistItems
                    .map { it.text }
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                val mergedContent = listOf(state.content, joined)
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                state.copy(isChecklist = false, content = mergedContent, checklistItems = emptyList())
            } else {
                val items = state.content
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .map { ChecklistItem(text = it.trim(), isChecked = false) }
                state.copy(isChecklist = true, content = "", checklistItems = items)
            }
        }
    }

    fun addChecklistItem() {
        hasUserEdited = true
        _uiState.update { it.copy(checklistItems = it.checklistItems + ChecklistItem()) }
    }

    fun updateChecklistItemText(index: Int, text: String) {
        hasUserEdited = true
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) items[index] = items[index].copy(text = text)
            state.copy(checklistItems = items)
        }
    }

    fun setChecklistItemChecked(index: Int, checked: Boolean) {
        hasUserEdited = true
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) items[index] = items[index].copy(isChecked = checked)
            state.copy(checklistItems = items)
        }
    }

    fun removeChecklistItem(index: Int) {
        hasUserEdited = true
        _uiState.update { state ->
            val items = state.checklistItems.toMutableList()
            if (index in items.indices) items.removeAt(index)
            state.copy(checklistItems = items)
        }
    }

    /** Persists the snapshot, inserting a new row the first time and updating it thereafter. */
    private suspend fun persist(state: EditorUiState) {
        if (state.isNewNote && state.isEmpty) return
        val note = state.toNote(createdAt)
        if (state.isNewNote) {
            val newId = createNote(note)
            createdAt = createdAt ?: java.time.LocalDateTime.now()
            _uiState.update { it.copy(noteId = newId, isNewNote = false) }
        } else {
            updateNote(note)
        }
    }

    fun archive() = viewModelScope.launch {
        val state = _uiState.value
        if (state.isNewNote && state.isEmpty) {
            _events.send(EditorEvent.DiscardedEmpty)
            return@launch
        }
        // Ensure the note exists before archiving it.
        if (state.isNewNote) persist(state)
        toggleArchive(_uiState.value.toNote(createdAt))
        _events.send(EditorEvent.Closed)
    }

    fun delete() = viewModelScope.launch {
        val state = _uiState.value
        if (!state.isNewNote) {
            // Soft-delete: the note goes to Trash and can be restored for 7 days.
            moveToTrash(state.toNote(createdAt))
        }
        _events.send(EditorEvent.Deleted)
    }

    /** Auto-saves on exit and reports whether the note was saved or discarded as empty. */
    fun onBackPressed() = viewModelScope.launch {
        val state = _uiState.value
        when {
            state.isEmpty -> {
                if (!state.isNewNote) deleteNote(state.toNote(createdAt))
                _events.send(EditorEvent.DiscardedEmpty)
            }

            hasUserEdited -> {
                persist(state)
                _events.send(EditorEvent.Saved)
            }

            else -> _events.send(EditorEvent.Closed)
        }
    }
}

private fun EditorUiState.toNote(createdAt: LocalDateTime?): Note = Note(
    id = noteId,
    title = title.trim(),
    content = content.trim(),
    color = color,
    isPinned = isPinned,
    isArchived = isArchived,
    createdAt = createdAt ?: LocalDateTime.now(),
    updatedAt = LocalDateTime.now(),
    labels = labels,
    reminderAt = reminderAt,
    isChecklist = isChecklist,
    // Drop fully-blank rows so an empty trailing input field is not persisted.
    checklistItems = checklistItems.filter { it.text.isNotBlank() || it.isChecked }
)
