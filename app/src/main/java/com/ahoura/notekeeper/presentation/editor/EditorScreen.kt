package com.ahoura.notekeeper.presentation.editor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.ui.components.ChecklistEditor
import com.ahoura.notekeeper.ui.components.LabelEditorSheet
import com.ahoura.notekeeper.ui.components.NoteColorPicker
import com.ahoura.notekeeper.ui.components.ReminderPickerDialog
import com.ahoura.notekeeper.ui.theme.contentColorFor
import com.ahoura.notekeeper.ui.theme.toComposeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val allLabels by viewModel.allLabels.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    var showColorPicker by remember { mutableStateOf(false) }
    var showLabelSheet by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val background by animateColorAsState(
        targetValue = state.color.toComposeColor(darkTheme),
        label = "editorBackground"
    )
    val onBackground = background.contentColorFor()

    // Collect exit effects and provide lightweight feedback before leaving the screen.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditorEvent.Saved ->
                    Toast.makeText(context, R.string.note_saved, Toast.LENGTH_SHORT).show()

                EditorEvent.DiscardedEmpty ->
                    Toast.makeText(context, R.string.note_discarded_empty, Toast.LENGTH_SHORT).show()

                EditorEvent.Deleted ->
                    Toast.makeText(context, R.string.note_deleted_toast, Toast.LENGTH_SHORT).show()

                EditorEvent.Closed -> Unit
            }
            onNavigateBack()
        }
    }

    BackHandler { viewModel.onBackPressed() }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = onBackground,
                    navigationIconContentColor = onBackground,
                    actionIconContentColor = onBackground
                ),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { viewModel.onBackPressed() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.action_pin)
                        )
                    }
                    IconButton(onClick = { showReminderPicker = true }) {
                        Icon(
                            imageVector = if (state.reminderAt != null) {
                                Icons.Filled.Notifications
                            } else {
                                Icons.Outlined.Notifications
                            },
                            contentDescription = stringResource(R.string.action_reminder)
                        )
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_archive)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.archive()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = {
                                menuExpanded = false
                                viewModel.delete()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(
                                        if (state.isChecklist) R.string.action_hide_checkboxes
                                        else R.string.action_checkboxes
                                    )
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                viewModel.toggleChecklistMode()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_change_color)) },
                            onClick = {
                                menuExpanded = false
                                showColorPicker = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_labels)) },
                            onClick = {
                                menuExpanded = false
                                showLabelSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_copy)) },
                            onClick = {
                                menuExpanded = false
                                val text = listOf(state.title, state.content)
                                    .filter { it.isNotBlank() }
                                    .joinToString("\n")
                                val manager =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                manager.setPrimaryClip(ClipData.newPlainText("note", text))
                                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            TransparentField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                hint = stringResource(R.string.title_hint),
                textColor = onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.isChecklist) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ChecklistEditor(
                        items = state.checklistItems,
                        onCheckedChange = viewModel::setChecklistItemChecked,
                        onTextChange = viewModel::updateChecklistItemText,
                        onRemove = viewModel::removeChecklistItem,
                        onAddItem = viewModel::addChecklistItem,
                        contentColor = onBackground
                    )
                }
            } else {
                TransparentField(
                    value = state.content,
                    onValueChange = viewModel::onContentChange,
                    hint = stringResource(R.string.content_hint),
                    textColor = onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
            }

            state.reminderAt?.let { reminder ->
                InputChip(
                    selected = true,
                    onClick = { showReminderPicker = true },
                    label = { Text(reminder.format(EDITOR_REMINDER_FORMAT)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(InputChipDefaults.IconSize)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_remove_reminder),
                            modifier = Modifier
                                .size(InputChipDefaults.IconSize)
                                .clickable { viewModel.setReminder(null) }
                        )
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (state.labels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.labels.forEach { label ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeLabel(label) },
                            label = { Text(label) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.action_remove_label),
                                    modifier = Modifier.size(InputChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        NoteColorPicker(
            selectedColor = state.color,
            onColorSelected = { viewModel.onColorChange(it) },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showLabelSheet) {
        LabelEditorSheet(
            assigned = state.labels,
            suggestions = allLabels,
            onToggleLabel = viewModel::toggleLabel,
            onCreateLabel = viewModel::addLabel,
            onDismiss = { showLabelSheet = false }
        )
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            initial = state.reminderAt,
            onConfirm = {
                viewModel.setReminder(it)
                showReminderPicker = false
                Toast.makeText(context, R.string.reminder_set, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showReminderPicker = false }
        )
    }
}

private val EDITOR_REMINDER_FORMAT: java.time.format.DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransparentField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(text = hint, color = textColor.copy(alpha = 0.5f), fontSize = fontSize)
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = textColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        )
    )
}
