package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.screens.PeriodNavigator

@Composable
fun PeriodNavBar(
    dateRange: DateRange,
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    navigator: PeriodNavigator = PeriodNavigator(),
    expenseTotal: Double = 0.0,
    totalColor: Color = MaterialTheme.colorScheme.error,
) {
    val navigable = navigator.isNavigable(dateRange)
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
                Text(text = label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "₹${"%.2f".format(kotlin.math.abs(expenseTotal))}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = totalColor,
                )
            }
            TextButton(onClick = onNext, enabled = navigable) { Text(">") }
        }
    }
}
