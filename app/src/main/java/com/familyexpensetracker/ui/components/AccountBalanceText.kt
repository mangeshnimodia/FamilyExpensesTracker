package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AccountBalanceText(balance: Double?) {
    if (balance == null) return
    val colour = if (balance >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    Column {
        Text(
            text = "Balance",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "\u20b9${"%.2f".format(kotlin.math.abs(balance))}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colour,
        )
    }
}
