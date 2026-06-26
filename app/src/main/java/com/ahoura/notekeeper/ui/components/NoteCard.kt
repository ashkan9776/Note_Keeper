package com.ahoura.notekeeper.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import com.ahoura.notekeeper.domain.model.Note
import com.ahoura.notekeeper.domain.model.NoteColor
import com.ahoura.notekeeper.ui.theme.NoteKeeperTheme
import com.ahoura.notekeeper.ui.theme.contentColorFor
import com.ahoura.notekeeper.ui.theme.toComposeColor

private const val MAX_PREVIEW_LINES = 8

/**
 * A single note card. Renders title, content preview, and label chips on the note's color, with a
 * pin badge when pinned and a checkmark overlay + scale-down when selected in multi-select mode.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()
) {
    val haptics = LocalHapticFeedback.current
    val cardColor = note.color.toComposeColor(darkTheme)
    val contentColor = cardColor.contentColorFor()
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Box {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = true)
                        )
                    } else {
                        // Keep the pin aligned right even when there is no title.
                        Box(modifier = Modifier.weight(1f))
                    }

                    val pinScale by animateFloatAsState(
                        targetValue = if (note.isPinned) 1f else 0f,
                        label = "pinScale"
                    )
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp).scale(pinScale)
                        )
                    }
                }

                if (note.isChecklist && note.checklistItems.isNotEmpty()) {
                    ChecklistPreview(note, contentColor)
                } else if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.85f),
                        maxLines = MAX_PREVIEW_LINES,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = if (note.title.isNotBlank()) 8.dp else 0.dp)
                    )
                }

                if (note.reminderAt != null) {
                    ReminderChip(reminderAt = note.reminderAt, contentColor = contentColor)
                }

                if (note.labels.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        note.labels.forEach { label -> LabelChip(label = label, contentColor = contentColor) }
                    }
                }
            }

            // Selection checkmark overlay with a scale-in animation.
            val checkScale by animateFloatAsState(
                targetValue = if (selectionMode && selected) 1f else 0f,
                label = "checkScale"
            )
            if (selectionMode && selected) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).scale(checkScale)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(2.dp).size(18.dp)
                    )
                }
            }
        }
    }
}

private const val MAX_CHECKLIST_PREVIEW = 6
private val reminderFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

/** Card preview for a checklist note: the first few items plus a "x of y" progress line. */
@Composable
private fun ChecklistPreview(note: Note, contentColor: Color) {
    Column(
        modifier = Modifier.padding(top = if (note.title.isNotBlank()) 8.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        note.checklistItems.take(MAX_CHECKLIST_PREVIEW).forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (item.isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = if (item.isChecked) 0.55f else 0.85f),
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        if (note.checklistItems.size > MAX_CHECKLIST_PREVIEW) {
            Text(
                text = "+${note.checklistItems.size - MAX_CHECKLIST_PREVIEW} more",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

/** Small pill showing the note's reminder time. */
@Composable
private fun ReminderChip(reminderAt: java.time.LocalDateTime, contentColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = contentColor.copy(alpha = 0.12f),
        contentColor = contentColor,
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = reminderAt.format(reminderFormatter),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun LabelChip(label: String, contentColor: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = contentColor.copy(alpha = 0.12f),
        contentColor = contentColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(name = "Note - Light", showBackground = true)
@Preview(name = "Note - Dark", showBackground = true, backgroundColor = 0xFF202124)
@Composable
private fun NoteCardPreview() {
    NoteKeeperTheme {
        NoteCard(
            note = Note(
                id = 1,
                title = "Groceries",
                content = "Milk, eggs, bread, coffee, and something sweet for the weekend.",
                color = NoteColor.YELLOW,
                isPinned = true,
                labels = listOf("Home", "Shopping")
            ),
            selected = false,
            selectionMode = false,
            onClick = {},
            onLongClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
