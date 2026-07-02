package com.familyexpensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TransactionsMenu(
    onAddExpense: () -> Unit,
    onFetchExpenses: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onAddExpense,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Expense")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFetchExpenses,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Expenses")
            }
        }
    }
}
