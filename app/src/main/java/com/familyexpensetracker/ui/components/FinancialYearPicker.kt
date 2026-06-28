package com.familyexpensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun FinancialYearPickerDialog(
    onYearSelected: (startYear: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    // Show a range of years around the current year
    val years = (currentYear - 5..currentYear + 2).reversed().toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Select Financial Year") },
        text = {
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                LazyColumn {
                    items(years) { startYear ->
                        val startYearShort = startYear % 100
                        val endYearShort = (startYear + 1) % 100
                        val display = "FY%02d-%02d".format(startYearShort, endYearShort)
                        ListItem(
                            headlineContent = { Text(display) },
                            modifier = Modifier.clickable { onYearSelected(startYear) }
                        )
                    }
                }
            }
        }
    )
}
