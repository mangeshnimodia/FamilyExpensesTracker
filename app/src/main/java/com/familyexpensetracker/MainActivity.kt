package com.familyexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.data.remote.GoogleSheetsDataSource
import com.familyexpensetracker.data.remote.GoogleSheetsServiceProvider
import com.familyexpensetracker.data.repository.ExpenseRepository
import com.familyexpensetracker.ui.screens.POCScreen
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

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
        val serviceProvider = GoogleSheetsServiceProvider(this)
        val sheetsService = serviceProvider.getSheetsService(email)
        val googleSheetsDataSource = GoogleSheetsDataSource(sheetsService)
        val repository = ExpenseRepository(googleSheetsDataSource)
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
