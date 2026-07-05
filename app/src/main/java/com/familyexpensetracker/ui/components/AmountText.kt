package com.familyexpensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.familyexpensetracker.ui.theme.AppColors
import com.familyexpensetracker.utils.IndianNumberFormatter

@Composable
fun AmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onColorCaptured: ((Color) -> Unit)? = null,
) {
    val color = if (amount >= 0) AppColors.incomeGreen else AppColors.expenseRed
    onColorCaptured?.invoke(color)
    Text(
        text = IndianNumberFormatter.formatAmount(amount),
        style = style,
        color = color,
        modifier = modifier,
    )
}
