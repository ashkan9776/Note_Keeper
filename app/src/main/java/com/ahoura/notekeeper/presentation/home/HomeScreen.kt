package com.ahoura.notekeeper.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.presentation.common.UiState
import com.ahoura.notekeeper.ui.components.BottomActionBar
import com.ahoura.notekeeper.ui.components.EmptyStateView
import com.ahoura.notekeeper.ui.components.NoteColorPicker
import com.ahoura.notekeeper.ui.components.NoteLayoutMode
import com.ahoura.notekeeper.ui.components.StaggeredNoteGrid
import com.ahoura.notekeeper.ui.theme.NoteKeeperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onSearchClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showColorPicker by remember { mutableStateOf(false) }
    var fabVisible by remember { mutableStateOf(true) }

    val nestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -4f) fabVisible = false
                else if (available.y > 4f) fabVisible = true
                return Offset.Zero
            }
        }
    }

    // Surface delete events as an undoable snackbar.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NotesDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_note_deleted),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = state.isSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topBar"
            ) { selectionMode ->
                if (selectionMode) {
                    SelectionTopBar(
                        count = state.selectionCount,
                        onClose = viewModel::clearSelection
                    )
                } else {
                    HomeTopBar(
                        layoutMode = state.layoutMode,
                        onToggleLayout = viewModel::toggleLayout,
                        onSearch = onSearchClick,
                        onArchive = onArchiveClick,
                        onTrash = onTrashClick,
                        onSettings = onSettingsClick
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible && !state.isSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                NewNoteFab(
                    noteCount = (state.notes as? UiState.Success)?.data?.size ?: 0,
                    onClick = onCreateNote
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomActionBar(
                visible = state.isSelectionMode,
                onPin = viewModel::pinSelected,
                onArchive = viewModel::archiveSelected,
                onChangeColor = { showColorPicker = true },
                onDelete = viewModel::deleteSelected
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!state.isSelectionMode && state.allLabels.isNotEmpty()) {
                LabelFilterRow(
                    labels = state.allLabels,
                    selectedLabel = state.selectedLabel,
                    onSelect = viewModel::selectLabel
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
            when (val notes = state.notes) {
                is UiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )

                is UiState.Error -> EmptyStateView(message = notes.message)

                is UiState.Success -> {
                    if (notes.data.isEmpty()) {
                        val message = if (state.selectedLabel != null) {
                            stringResource(R.string.empty_label_filter, state.selectedLabel!!)
                        } else {
                            stringResource(R.string.empty_home)
                        }
                        EmptyStateView(message = message)
                    } else {
                        StaggeredNoteGrid(
                            pinned = state.pinned,
                            others = state.others,
                            layoutMode = state.layoutMode,
                            selectedIds = state.selectedIds,
                            selectionMode = state.isSelectionMode,
                            onNoteClick = { note ->
                                if (state.isSelectionMode) viewModel.toggleSelection(note.id)
                                else onNoteClick(note.id)
                            },
                            onNoteLongClick = { note -> viewModel.onNoteLongPress(note.id) },
                            onArchiveSwipe = viewModel::archiveNote,
                            modifier = Modifier.fillMaxSize().nestedScroll(nestedScroll)
                        )
                    }
                }
            }
            }
        }
    }

    if (showColorPicker) {
        NoteColorPicker(
            selectedColor = com.ahoura.notekeeper.domain.model.NoteColor.DEFAULT,
            onColorSelected = { color ->
                viewModel.changeColorForSelected(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    layoutMode: NoteLayoutMode,
    onToggleLayout: () -> Unit,
    onSearch: () -> Unit,
    onArchive: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_search))
            }
            IconButton(onClick = onToggleLayout) {
                if (layoutMode == NoteLayoutMode.GRID) {
                    Icon(
                        Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = stringResource(R.string.action_view_list)
                    )
                } else {
                    Icon(
                        Icons.Filled.GridView,
                        contentDescription = stringResource(R.string.action_view_grid)
                    )
                }
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_archive)) },
                    leadingIcon = { Icon(Icons.Filled.Archive, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onArchive()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.trash_title)) },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onTrash()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_settings)) },
                    leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onSettings()
                    }
                )
}
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelFilterRow(
    labels: List<String>,
    selectedLabel: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "__all__") {
            FilterChip(
                selected = selectedLabel == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.filter_all)) }
            )
        }
        items(labels, key = { it }) { label ->
            FilterChip(
                selected = label == selectedLabel,
                onClick = { onSelect(label) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(count: Int, onClose: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close))
            }
        },
        title = { Text(stringResource(R.string.selection_count, count)) }
    )
}

@Composable
private fun NewNoteFab(noteCount: Int, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (noteCount > 0) {
                Badge { Text(noteCount.toString()) }
            }
        }
    ) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.fab_new_note)) }
        )
    }
}

@Preview(name = "Home - Light", showBackground = true)
@Preview(name = "Home - Dark", showBackground = true, backgroundColor = 0xFF202124)
@Composable
private fun HomeScreenPreview() {
    NoteKeeperTheme {
        // Preview renders the empty state since no Hilt graph is available here.
        EmptyStateView(message = "Your notes appear here")
    }
}
