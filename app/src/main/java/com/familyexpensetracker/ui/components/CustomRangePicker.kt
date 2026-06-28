package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRangePicker(
    onRangeSelected: (startDate: Date, endDate: Date) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        // Convert UTC to local if needed, but for range it might be fine
                        // Let's use similar logic as AppDatePicker to be safe
                        val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = startMillis
                        }
                        val localStart = Calendar.getInstance().apply {
                            set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = endMillis
                        }
                        val localEnd = Calendar.getInstance().apply {
                            set(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }

                        onRangeSelected(localStart.time, localEnd.time)
                        onDismiss()
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select Date Range",
                    modifier = Modifier.padding(16.dp)
                )
            },
            modifier = Modifier.weight(1f)
        )
    }
}
