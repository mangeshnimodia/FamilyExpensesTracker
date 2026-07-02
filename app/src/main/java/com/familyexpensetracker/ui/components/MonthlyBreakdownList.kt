package com.familyexpensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.ui.theme.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun MonthlyBreakdownList(
    summaries: List<MonthSummary>,
    onMonthClick: (DateRange.Month) -> Unit,
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
        items(summaries.size) { index ->
            val summary = summaries[index]
            val label = monthFormat.format(
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, summary.year)
                    set(Calendar.MONTH, summary.month)
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            )
            MonthSummaryRow(
                label = label,
                summary = summary,
                onClick = { onMonthClick(DateRange.Month(summary.year, summary.month)) },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun MonthSummaryRow(
    label: String,
    summary: MonthSummary,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
        )
        val amountColor = if (summary.totalAmount < 0) AppColors.expenseRed else AppColors.summaryGreen
        Text(
            text = "%.2f".format(abs(summary.totalAmount)),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor,
        )
    }
}
