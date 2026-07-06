package com.familyexpensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.screens.PeriodNavigator
import java.util.Calendar
import java.util.Date

@Composable
fun PeriodNavBar(
    dateRange: DateRange,
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    navigator: PeriodNavigator = PeriodNavigator(),
    expenseTotal: Double = 0.0,
    onDateSelected: ((Date) -> Unit)? = null,
) {
    val navigable = navigator.isNavigable(dateRange)
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPrevious, enabled = navigable) { Text("<") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = label, style = MaterialTheme.typography.titleMedium)
                    if (dateRange is DateRange.Day && onDateSelected != null) {
                        val currentDate = dateRange.date
                        IconButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { time = currentDate }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(year, month, day, 0, 0, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.time
                                        onDateSelected(picked)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH),
                                ).show()
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick date",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                AmountText(
                    amount = expenseTotal,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            TextButton(onClick = onNext, enabled = navigable) { Text(">") }
        }
    }
}
