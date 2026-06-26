package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R

/**
 * Modal bottom sheet for assigning labels to the open note. The user can type a new label to create
 * it, or toggle any of the labels already used elsewhere. Mirrors [NoteColorPicker]'s sheet styling.
 *
 * @param assigned labels currently on the note (checked).
 * @param suggestions every label used across notes, shown as a checkable list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelEditorSheet(
    assigned: List<String>,
    suggestions: List<String>,
    onToggleLabel: (String) -> Unit,
    onCreateLabel: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var input by remember { mutableStateOf(TextFieldValue("")) }

    // Union of suggestions and already-assigned labels, so a note's own labels always appear.
    val rows = remember(suggestions, assigned) {
        (suggestions + assigned).distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
    }

    fun submit() {
        val text = input.text.trim()
        if (text.isNotEmpty()) {
            onCreateLabel(text)
            input = TextFieldValue("")
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp)) {
            Text(
                text = stringResource(R.string.labels_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.label_hint)) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { submit() }),
                trailingIcon = {
                    if (input.text.isNotBlank()) {
                        IconButton(onClick = { submit() }) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(R.string.action_add_label)
                            )
                        }
                    }
                }
            )

            if (rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_labels_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(top = 8.dp)) {
                    items(rows, key = { it }) { label ->
                        val checked = assigned.any { it.equals(label, ignoreCase = true) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { onToggleLabel(label) })
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
