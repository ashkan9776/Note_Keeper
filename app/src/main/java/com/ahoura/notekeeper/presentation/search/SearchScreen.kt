package com.ahoura.notekeeper.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.EmptyStateView
import com.ahoura.notekeeper.ui.components.NoteLayoutMode
import com.ahoura.notekeeper.ui.components.SearchBar
import com.ahoura.notekeeper.ui.components.StaggeredNoteGrid

@Composable
fun SearchScreen(
    onNoteClick: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val recents by viewModel.recentSearches.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            if (recents.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = recents, key = { it }) { recent ->
                        AssistChip(
                            onClick = { viewModel.applyRecentSearch(recent) },
                            label = { Text(recent) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = results) {
                    is UiState.Loading -> Unit

                    is UiState.Error -> EmptyStateView(message = state.message)

                    is UiState.Success -> when {
                        query.isBlank() -> EmptyStateView(
                            message = stringResource(R.string.search_prompt)
                        )

                        state.data.isEmpty() -> EmptyStateView(
                            message = stringResource(R.string.search_no_results)
                        )

                        else -> StaggeredNoteGrid(
                            pinned = emptyList(),
                            others = state.data,
                            layoutMode = NoteLayoutMode.GRID,
                            selectedIds = emptySet(),
                            selectionMode = false,
                            onNoteClick = { onNoteClick(it.id) },
                            onNoteLongClick = { onNoteClick(it.id) },
                            onArchiveSwipe = {},
                            showSectionHeaders = false
                        )
                    }
                }
            }
        }
    }
}
