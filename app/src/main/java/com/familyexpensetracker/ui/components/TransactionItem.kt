package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.Transaction

@Composable
fun TransactionItem(txn: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${txn.date} - ${txn.description}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Amount: ₹${txn.amount} | Account: ${txn.account}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
