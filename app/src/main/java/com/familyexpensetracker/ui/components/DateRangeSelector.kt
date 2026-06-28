package com.familyexpensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.familyexpensetracker.data.model.DateRange
import java.util.*

@Composable
fun DateRangeSelector(
    onRangeSelected: (DateRange) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }

    if (selectedType == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Range Type") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Day") },
                        modifier = Modifier.clickable { selectedType = "Day" }
                    )
                    ListItem(
                        headlineContent = { Text("Month") },
                        modifier = Modifier.clickable { selectedType = "Month" }
                    )
                    ListItem(
                        headlineContent = { Text("Financial Year") },
                        modifier = Modifier.clickable { selectedType = "Year" }
                    )
                    ListItem(
                        headlineContent = { Text("Custom Range") },
                        modifier = Modifier.clickable { selectedType = "Custom" }
                    )
                }
            }
        )
    } else {
        when (selectedType) {
            "Day" -> {
                AppDatePicker(
                    initialDate = Date(),
                    onDateSelected = { 
                        onRangeSelected(DateRange.Day(it))
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
            "Month" -> {
                MonthYearPickerDialog(
                    onMonthSelected = { year, month ->
                        onRangeSelected(DateRange.Month(year, month))
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
            "Year" -> {
                FinancialYearPickerDialog(
                    onYearSelected = { year ->
                        onRangeSelected(DateRange.FinancialYear(year))
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
            "Custom" -> {
                CustomRangePicker(
                    onRangeSelected = { start, end ->
                        onRangeSelected(DateRange.Custom(start, end))
                        onDismiss()
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }
}
