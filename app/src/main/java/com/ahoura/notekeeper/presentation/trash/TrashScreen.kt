package com.ahoura.notekeeper.presentation.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.EmptyStateView
import com.ahoura.notekeeper.ui.components.NoteLayoutMode
import com.ahoura.notekeeper.ui.components.StaggeredNoteGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Note the user tapped, awaiting a restore / delete-forever choice.
    var actionNote by remember { mutableStateOf<Note?>(null) }
    var showEmptyConfirm by remember { mutableStateOf(false) }

    val notes = (state as? UiState.Success)?.data.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (notes.isNotEmpty()) {
                        IconButton(onClick = { showEmptyConfirm = true }) {
                            Icon(
                                Icons.Filled.DeleteForever,
                                contentDescription = stringResource(R.string.action_empty_trash)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (val s = state) {
                    is UiState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    is UiState.Error -> EmptyStateView(message = s.message)

                    is UiState.Success -> if (s.data.isEmpty()) {
                        EmptyStateView(message = stringResource(R.string.empty_trash))
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = stringResource(R.string.trash_retention_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            StaggeredNoteGrid(
                                pinned = emptyList(),
                                others = s.data,
                                layoutMode = NoteLayoutMode.GRID,
                                selectedIds = emptySet(),
                                selectionMode = false,
                                onNoteClick = { actionNote = it },
                                onNoteLongClick = { actionNote = it },
                                onArchiveSwipe = {},
                                showSectionHeaders = false
                            )
                        }
                    }
                }
            }
        }
    }

    actionNote?.let { note ->
        AlertDialog(
            onDismissRequest = { actionNote = null },
            title = { Text(note.title.ifBlank { stringResource(R.string.trash_untitled) }) },
            text = { Text(stringResource(R.string.trash_action_prompt)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restore(note)
                    actionNote = null
                }) {
                    Icon(Icons.Filled.RestoreFromTrash, contentDescription = null)
                    Text(
                        text = stringResource(R.string.action_restore),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deleteForever(note)
                    actionNote = null
                }) {
                    Text(
                        text = stringResource(R.string.action_delete_forever),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text(stringResource(R.string.action_empty_trash)) },
            text = { Text(stringResource(R.string.trash_empty_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emptyTrashNow()
                    showEmptyConfirm = false
                }) {
                    Text(
                        text = stringResource(R.string.action_delete_forever),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
