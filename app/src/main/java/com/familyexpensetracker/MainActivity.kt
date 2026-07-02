package com.familyexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.data.remote.GoogleSheetsServiceProvider
import com.familyexpensetracker.ui.screens.TransactionsScreen
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel

class MainActivity : ComponentActivity() {
    private var transactionVM: TransactionViewModel? by mutableStateOf(null)
    private var categoryVM: CategoryViewModel? by mutableStateOf(null)
    private var accountVM: AccountViewModel? by mutableStateOf(null)
    private var searchVM: SearchViewModel? by mutableStateOf(null)

    private val authManager = GoogleAuthManager(
        activity = this,
        onEmailReady = { email -> initializeSheets(email) },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager.register()
        authManager.checkExisting(this)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val txVM = transactionVM
                    val catVM = categoryVM
                    val accVM = accountVM
                    val srchVM = searchVM
                    if (txVM != null && catVM != null && accVM != null && srchVM != null) {
                        TransactionsScreen(txVM, catVM, accVM, srchVM)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Button(onClick = { authManager.startSignIn() }) {
                                Text("Sign In with Google")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initializeSheets(email: String) {
        val sheetsService = GoogleSheetsServiceProvider(this).getSheetsService(email)
        val deps = AppDependencies(this, sheetsService)
        transactionVM = deps.transactionViewModel
        categoryVM = deps.categoryViewModel
        accountVM = deps.accountViewModel
        searchVM = deps.searchViewModel
    }
}
