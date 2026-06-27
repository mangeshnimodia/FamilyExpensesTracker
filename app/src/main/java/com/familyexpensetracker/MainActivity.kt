package com.familyexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.GoogleSheetsHelper
import com.familyexpensetracker.data.repository.ExpenseRepository
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import java.util.UUID

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ExpenseViewModel

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            account?.email?.let { email ->
                initializeSheets(email)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastAccount != null && lastAccount.email != null) {
            initializeSheets(lastAccount.email!!)
        } else {
            startSignIn()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (::viewModel.isInitialized) {
                        POCScreen(viewModel)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { startSignIn() }) {
                                Text("Sign In with Google")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets"))
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(client.signInIntent)
    }

    private fun initializeSheets(email: String) {
        val googleSheetsHelper = GoogleSheetsHelper(this, email)
        val repository = ExpenseRepository(googleSheetsHelper)
        viewModel = ExpenseViewModel(repository)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    POCScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun POCScreen(viewModel: ExpenseViewModel) {
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
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

@Composable
private fun TransactionItem(txn: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${txn.date} - ${txn.description}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Amount: ₹${txn.amount} | Account: ${txn.account}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
