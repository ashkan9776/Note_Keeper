package com.ahoura.notekeeper.presentation.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.EmptyStateView
import com.ahoura.notekeeper.ui.components.NoteLayoutMode
import com.ahoura.notekeeper.ui.components.StaggeredNoteGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNoteClick: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val s = state) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is UiState.Error -> EmptyStateView(message = s.message)

                is UiState.Success -> if (s.data.isEmpty()) {
                    EmptyStateView(message = stringResource(R.string.empty_archive))
                } else {
                    // Archived notes are unarchived by swiping (reuses the swipe-to-archive gesture).
                    StaggeredNoteGrid(
                        pinned = emptyList(),
                        others = s.data,
                        layoutMode = NoteLayoutMode.GRID,
                        selectedIds = emptySet(),
                        selectionMode = false,
                        onNoteClick = { onNoteClick(it.id) },
                        onNoteLongClick = { onNoteClick(it.id) },
                        onArchiveSwipe = viewModel::unarchive,
                        showSectionHeaders = false
                    )
                }
            }
        }
    }
}
