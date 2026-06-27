package com.familyexpensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.ui.components.TransactionItem
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import java.util.*

@Composable
fun POCScreen(viewModel: ExpenseViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.fetchTransactions() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Transactions")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val newTxn = Transaction(
                        txnId = UUID.randomUUID().toString(),
                        date = "2026/06/27",
                        amount = 100.0,
                        category = "Food",
                        subcategory = "Snacks",
                        paymentMethod = "Cash",
                        description = "POC Entry",
                        account = "Wallet"
                    )
                    viewModel.addTransaction(newTxn)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add POC Entry (₹100)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(transactions) { txn ->
                    TransactionItem(txn)
                }
            }
        }
    }
}
