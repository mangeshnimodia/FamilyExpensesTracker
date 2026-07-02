package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AccountBalanceText(balance: Double?) {
    val amount = balance ?: 0.0
    val colour = if (amount >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
    Column(modifier = Modifier.testTag("accountBalanceText")) {
        Text(
            text = "Balance",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "\u20b9${"%.2f".format(kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colour,
        )
    }
}
