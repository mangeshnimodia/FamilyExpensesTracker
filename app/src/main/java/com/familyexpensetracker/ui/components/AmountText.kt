package com.familyexpensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.familyexpensetracker.ui.theme.AppColors
import com.familyexpensetracker.utils.IndianNumberFormatter
import kotlin.math.abs

val AmountColorSemanticsKey = SemanticsPropertyKey<Color>("AmountColor")
var SemanticsPropertyReceiver.amountColor: Color by AmountColorSemanticsKey

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
        text = IndianNumberFormatter.formatAmount(abs(amount)),
        style = style,
        color = color,
        modifier = modifier.semantics { amountColor = color },
    )
}
