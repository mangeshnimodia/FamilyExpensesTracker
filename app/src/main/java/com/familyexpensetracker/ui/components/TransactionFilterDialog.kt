package com.familyexpensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.data.model.TransactionFilter

@Composable
fun TransactionFilterDialog(
    currentFilter: TransactionFilter,
    availableAccounts: List<String>,
    onFilterApplied: (TransactionFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAccounts by remember { 
        mutableStateOf(
            if (currentFilter.selectedAccounts.isEmpty()) {
                if (availableAccounts.contains("Passbook")) listOf("Passbook") else emptyList()
            } else {
                currentFilter.selectedAccounts
            }
        )
    }
    var selectedType by remember { mutableStateOf(currentFilter.type) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Transactions") },
        confirmButton = {
            TextButton(onClick = {
                onFilterApplied(TransactionFilter(selectedAccounts, selectedType))
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Accounts", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.heightIn(max = 200.dp)) {
                    LazyColumn {
                        items(availableAccounts) { account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAccounts = if (selectedAccounts.contains(account)) {
                                            selectedAccounts - account
                                        } else {
                                            selectedAccounts + account
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedAccounts.contains(account),
                                    onCheckedChange = null // Handled by row click
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(account)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Transaction Type", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                FilterTypeOption("Income", FilterType.INCOME, selectedType) { selectedType = it }
                FilterTypeOption("Expense", FilterType.EXPENSE, selectedType) { selectedType = it }
                FilterTypeOption("Balance", FilterType.BALANCE, selectedType) { selectedType = it }
            }
        }
    )
}

@Composable
private fun FilterTypeOption(
    label: String,
    type: FilterType,
    selectedType: FilterType,
    onSelected: (FilterType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(type) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedType == type,
            onClick = null // Handled by row click
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}
