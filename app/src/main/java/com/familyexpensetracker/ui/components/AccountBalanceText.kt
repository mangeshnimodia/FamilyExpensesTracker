package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.familyexpensetracker.ui.theme.AppColors
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AccountBalanceText(balance: Double?) {
    val amount = balance ?: 0.0
    val color = if (amount >= 0) AppColors.incomeGreen else MaterialTheme.colorScheme.error
    Column(modifier = Modifier.testTag("accountBalanceText")) {
        Text(
            text = "Balance",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "\u20b9${"%.2f".format(kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}
