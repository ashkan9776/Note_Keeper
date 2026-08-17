package com.ahoura.notekeeper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ahoura.notekeeper.R
import saman.zamani.persiandate.PersianDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Composable
fun PersianDatePickerDialog(
    initial: LocalDateTime,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val date = Date.from(initial.atZone(ZoneId.systemDefault()).toInstant())
    val pDate = PersianDate(date)

    var year by remember { mutableIntStateOf(pDate.shYear) }
    var month by remember { mutableIntStateOf(pDate.shMonth) }
    var day by remember { mutableIntStateOf(pDate.shDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NumberPicker(
                        label = "روز",
                        value = day,
                        range = 1..31,
                    ) { day = it }
                    NumberPicker(
                        label = "ماه",
                        value = month,
                        range = 1..12,
                    ) { month = it }
                    NumberPicker(
                        label = "سال",
                        value = year,
                        range = 1400..1410,
                    ) { year = it }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newPDate = PersianDate()
                    newPDate.shYear = year
                    newPDate.shMonth = month
                    newPDate.shDay = day
                    newPDate.hour = initial.hour
                    newPDate.minute = initial.minute

                    val gregorianDate = newPDate.toDate()
                    onConfirm(LocalDateTime.ofInstant(gregorianDate.toInstant(), ZoneId.systemDefault()))
                },
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun NumberPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        TextButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
            Text("+")
        }
        Text(text = value.toString().toPersianDigits(), style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
            Text("-")
        }
    }
}
