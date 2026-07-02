package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.ui.theme.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.Transaction
import kotlin.math.abs

@Composable
fun TransactionItem(
    txn: Transaction,
    onDelete: () -> Unit = {},
    onEdit: (Transaction) -> Unit = {},
    onCopy: (Transaction) -> Unit = {},
) {
    val amountColor = if (txn.amount < 0) MaterialTheme.colorScheme.error else AppColors.incomeGreen
    val categoryText = if (txn.subcategory.isNotBlank()) "${txn.category}/${txn.subcategory}" else txn.category

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            // Row 1: Category | Date | Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = txn.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(
                    text = "\u20B9${"%.2f".format(abs(txn.amount))}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
            // Row 2: Description | Edit / Copy / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = txn.description.ifBlank { "" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onEdit(txn) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(
                    onClick = { onCopy(txn) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.tertiary)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
