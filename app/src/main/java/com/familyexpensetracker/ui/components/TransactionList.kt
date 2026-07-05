package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onEdit: (Transaction) -> Unit,
    onCopy: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = AppConstants.FAB_CLEARANCE_DP.dp)) {
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
