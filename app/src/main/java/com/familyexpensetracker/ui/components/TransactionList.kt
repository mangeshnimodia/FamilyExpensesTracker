package com.familyexpensetracker.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.familyexpensetracker.data.model.Transaction

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onEdit: (Transaction) -> Unit,
    onCopy: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    LazyColumn {
        items(transactions.size) { index ->
            val txn = transactions[index]
            TransactionItem(
                txn = txn,
                onEdit = onEdit,
                onCopy = onCopy,
                onDelete = { onDelete(txn) },
            )
            HorizontalDivider()
        }
    }
}
