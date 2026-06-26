package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

private enum class Step { DATE, TIME }

/**
 * Two-step reminder picker: choose a date, then a time. [initial] pre-fills both. Calls [onConfirm]
 * with the combined [LocalDateTime] once the time is set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    initial: LocalDateTime?,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val seed = initial ?: LocalDateTime.now().plusHours(1).withMinute(0)
    var step by remember { mutableStateOf(Step.DATE) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = seed.toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = seed.hour,
        initialMinute = seed.minute,
        is24Hour = false
    )

    when (step) {
        Step.DATE -> DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { step = Step.TIME },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }

        Step.TIME -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.reminder_dialog_title)) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val dateMillis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    onConfirm(LocalDateTime.of(date, time))
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}
