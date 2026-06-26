package com.ahoura.notekeeper.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.usecase.SearchNotesUseCase
import com.ahoura.notekeeper.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 300L
private const val MAX_RECENT_SEARCHES = 8

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNotes: SearchNotesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Session-only recent searches (not persisted, per spec).
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val results: StateFlow<UiState<List<Note>>> = _query
        .debounce(SEARCH_DEBOUNCE_MS)
        .flatMapLatest { q -> searchNotes(q) }
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Search failed")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Success(emptyList()))

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onSearchSubmitted() {
        val q = _query.value.trim()
        if (q.isEmpty()) return
        _recentSearches.update { current ->
            (listOf(q) + current.filterNot { it.equals(q, ignoreCase = true) }).take(MAX_RECENT_SEARCHES)
        }
    }

    fun applyRecentSearch(value: String) {
        _query.value = value
    }
}
