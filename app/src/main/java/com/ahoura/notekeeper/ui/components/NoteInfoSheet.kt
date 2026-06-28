package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.presentation.editor.EditorUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val INFO_DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")

/**
 * A bottom sheet summarising live statistics for the note open in the editor: word count,
 * character count, estimated reading time, and the creation date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInfoSheet(
    state: EditorUiState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.note_info_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            InfoRow(
                icon = Icons.Filled.Spellcheck,
                label = stringResource(R.string.note_info_words),
                value = state.wordCount.toString()
            )
            InfoRow(
                icon = Icons.Filled.TextFields,
                label = stringResource(R.string.note_info_characters),
                value = state.characterCount.toString()
            )
            if (state.readingMinutes > 0) {
                InfoRow(
                    icon = Icons.Filled.Schedule,
                    label = stringResource(R.string.note_info_reading_time),
                    value = pluralStringResource(
                        R.plurals.note_info_minutes,
                        state.readingMinutes,
                        state.readingMinutes
                    )
                )
            }
            InfoRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.note_info_created),
                value = state.createdAt.formatOrNotSaved()
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LocalDateTime?.formatOrNotSaved(): String =
    this?.format(INFO_DATE_FORMAT) ?: stringResource(R.string.note_info_not_saved)
