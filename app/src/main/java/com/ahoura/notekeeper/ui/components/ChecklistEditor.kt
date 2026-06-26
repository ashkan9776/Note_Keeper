package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahoura.notekeeper.R
import com.ahoura.notekeeper.domain.model.ChecklistItem

/**
 * Editable checklist used inside the editor: a checkbox + inline text field + remove button per
 * item, with an "add item" affordance at the bottom. Colors are passed in so the list blends with
 * the note's background tint.
 */
@Composable
fun ChecklistEditor(
    items: List<ChecklistItem>,
    onCheckedChange: (index: Int, checked: Boolean) -> Unit,
    onTextChange: (index: Int, text: String) -> Unit,
    onRemove: (index: Int) -> Unit,
    onAddItem: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onCheckedChange(index, it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = contentColor,
                        uncheckedColor = contentColor.copy(alpha = 0.6f),
                        checkmarkColor = MaterialTheme.colorScheme.surface
                    )
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = item.text,
                    onValueChange = { onTextChange(index, it) },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = if (item.isChecked) contentColor.copy(alpha = 0.5f) else contentColor,
                        fontSize = 16.sp,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(contentColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                IconButton(onClick = { onRemove(index) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_remove_item),
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        TextButton(
            onClick = onAddItem,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.checklist_add_item),
                color = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
