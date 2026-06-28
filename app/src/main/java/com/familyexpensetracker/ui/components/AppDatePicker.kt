package com.familyexpensetracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePicker(
    initialDate: Date?,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time ?: System.currentTimeMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        // Convert UTC millis to Local date to avoid "past date" issues
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = utcMillis
                        val localCalendar = Calendar.getInstance()
                        localCalendar.set(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        onDateSelected(localCalendar.time)
                    }
                    onDismiss()
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
