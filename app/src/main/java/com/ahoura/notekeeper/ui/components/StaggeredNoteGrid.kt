package com.ahoura.notekeeper.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.domain.model.Note

/** Which way the home/archive lists are laid out. */
enum class NoteLayoutMode { GRID, LIST }

/**
 * Renders [pinned] and [others] note groups with subtle section headers, animating between the
 * masonry [NoteLayoutMode.GRID] and single-column [NoteLayoutMode.LIST]. List mode supports
 * swipe-to-archive; grid mode does not (matching the spec).
 */
@Composable
fun StaggeredNoteGrid(
    pinned: List<Note>,
    others: List<Note>,
    layoutMode: NoteLayoutMode,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onArchiveSwipe: (Note) -> Unit,
    modifier: Modifier = Modifier,
    showSectionHeaders: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    listState: LazyListState = rememberLazyListState()
) {
    AnimatedContent(
        targetState = layoutMode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "layoutMode",
        modifier = modifier
    ) { mode ->
        when (mode) {
            NoteLayoutMode.GRID -> GridContent(
                pinned = pinned,
                others = others,
                selectedIds = selectedIds,
                selectionMode = selectionMode,
                onNoteClick = onNoteClick,
                onNoteLongClick = onNoteLongClick,
                showSectionHeaders = showSectionHeaders,
                contentPadding = contentPadding,
                state = gridState
            )

            NoteLayoutMode.LIST -> ListContent(
                pinned = pinned,
                others = others,
                selectedIds = selectedIds,
                selectionMode = selectionMode,
                onNoteClick = onNoteClick,
                onNoteLongClick = onNoteLongClick,
                onArchiveSwipe = onArchiveSwipe,
                showSectionHeaders = showSectionHeaders,
                contentPadding = contentPadding,
                state = listState
            )
        }
    }
}

@Composable
private fun GridContent(
    pinned: List<Note>,
    others: List<Note>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    showSectionHeaders: Boolean,
    contentPadding: PaddingValues,
    state: LazyStaggeredGridState
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showSectionHeaders && pinned.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) { SectionHeader("PINNED") }
        }
        items(items = pinned, key = { it.id }) { note ->
            NoteCard(
                note = note,
                selected = note.id in selectedIds,
                selectionMode = selectionMode,
                onClick = { onNoteClick(note) },
                onLongClick = { onNoteLongClick(note) },
                modifier = Modifier.animateItem()
            )
        }
        if (showSectionHeaders && pinned.isNotEmpty() && others.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) { SectionHeader("OTHERS") }
        }
        items(items = others, key = { it.id }) { note ->
            NoteCard(
                note = note,
                selected = note.id in selectedIds,
                selectionMode = selectionMode,
                onClick = { onNoteClick(note) },
                onLongClick = { onNoteLongClick(note) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ListContent(
    pinned: List<Note>,
    others: List<Note>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onArchiveSwipe: (Note) -> Unit,
    showSectionHeaders: Boolean,
    contentPadding: PaddingValues,
    state: LazyListState
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showSectionHeaders && pinned.isNotEmpty()) {
            item(key = "header_pinned") { SectionHeader("PINNED") }
        }
        items(items = pinned, key = { it.id }) { note ->
            SwipeableNoteRow(note, selectedIds, selectionMode, onNoteClick, onNoteLongClick, onArchiveSwipe, Modifier.animateItem())
        }
        if (showSectionHeaders && pinned.isNotEmpty() && others.isNotEmpty()) {
            item(key = "header_others") { SectionHeader("OTHERS") }
        }
        items(items = others, key = { it.id }) { note ->
            SwipeableNoteRow(note, selectedIds, selectionMode, onNoteClick, onNoteLongClick, onArchiveSwipe, Modifier.animateItem())
        }
    }
}

@Composable
private fun SwipeableNoteRow(
    note: Note,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    onArchiveSwipe: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onArchiveSwipe(note)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        NoteCard(
            note = note,
            selected = note.id in selectedIds,
            selectionMode = selectionMode,
            onClick = { onNoteClick(note) },
            onLongClick = { onNoteLongClick(note) }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}
