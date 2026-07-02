package com.familyexpensetracker

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.familyexpensetracker.data.local.LocalAccountsDataSource
import com.familyexpensetracker.data.local.LocalCategoriesDataSource
import com.familyexpensetracker.data.remote.AddTransactionDataSource
import com.familyexpensetracker.data.remote.DeleteTransactionDataSource
import com.familyexpensetracker.data.remote.UpdateTransactionDataSource
import com.familyexpensetracker.data.remote.FetchAccountsDataSource
import com.familyexpensetracker.data.remote.FetchCategoriesDataSource
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource
import com.familyexpensetracker.data.remote.SearchTransactionsDataSource
import com.familyexpensetracker.data.remote.GoogleSheetsServiceProvider
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.UpdateTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountsRepository
import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import com.familyexpensetracker.data.repository.FetchAccountBalanceRepository
import com.familyexpensetracker.data.repository.SearchTransactionsRepository
import com.familyexpensetracker.ui.screens.TransactionsScreen
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import com.familyexpensetracker.utils.AppConstants
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

class MainActivity : ComponentActivity() {
    private var transactionVM: TransactionViewModel? by mutableStateOf(null)
    private var categoryVM: CategoryViewModel? by mutableStateOf(null)
    private var accountVM: AccountViewModel? by mutableStateOf(null)
    private var searchVM: SearchViewModel? by mutableStateOf(null)

    private val accountPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            email?.let { authorizeAndInitialize(it) }
        }
    }

    private val authorizeLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val authorizationResult = Identity.getAuthorizationClient(this)
                        .getAuthorizationResultFromIntent(result.data)
                    
                    val email = authorizationResult.toGoogleSignInAccount()?.email
                    if (email != null) {
                        initializeSheets(email)
                    } else {
                        val accounts = AccountManager.get(this).getAccountsByType(AppConstants.GOOGLE_ACCOUNT_TYPE)
                        if (accounts.isNotEmpty()) {
                            initializeSheets(accounts[0].name)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accounts = AccountManager.get(this).getAccountsByType(AppConstants.GOOGLE_ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            checkExistingAuthorization(accounts[0].name)
        }

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
                            Button(onClick = { startSignIn() }) {
                                Text("Sign In with Google")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkExistingAuthorization(email: String) {
        val requestedScopes = AppConstants.SCOPES.map { Scope(it) }
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .setAccount(Account(email, AppConstants.GOOGLE_ACCOUNT_TYPE))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (!result.hasResolution()) {
                    initializeSheets(email)
                }
            }
            .addOnFailureListener {
            }
    }

    private fun startSignIn() {
        val intent = AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf(AppConstants.GOOGLE_ACCOUNT_TYPE),
            null,
            null,
            null,
            null,
        )
        accountPickerLauncher.launch(intent)
    }

    private fun authorizeAndInitialize(email: String) {
        val requestedScopes = AppConstants.SCOPES.map { Scope(it) }
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .setAccount(Account(email, AppConstants.GOOGLE_ACCOUNT_TYPE))
            .build()

        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    authorizeLauncher.launch(
                        IntentSenderRequest.Builder(pendingIntent!!.intentSender).build(),
                    )
                } else {
                    initializeSheets(email)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun initializeSheets(email: String) {
        val serviceProvider = GoogleSheetsServiceProvider(this)
        val sheetsService = serviceProvider.getSheetsService(email)
        val fetchDataSource = FetchTransactionsDataSource(sheetsService)
        val fetchRepository = FetchTransactionsRepository(fetchDataSource)
        
        val addDataSource = AddTransactionDataSource(sheetsService)
        val addRepository = AddTransactionRepository(addDataSource)
        
        val deleteDataSource = DeleteTransactionDataSource(sheetsService)
        val deleteRepository = DeleteTransactionRepository(deleteDataSource)

        val updateDataSource = UpdateTransactionDataSource(sheetsService)
        val updateRepository = UpdateTransactionRepository(updateDataSource)

        val expenseCategoriesDataSource = FetchCategoriesDataSource(sheetsService, AppConstants.RANGE_EXPENSE_CATEGORIES)
        val localExpenseCategoriesDataSource = LocalCategoriesDataSource(this, AppConstants.KEY_EXPENSE_CATEGORIES_MAP)
        val expenseCategoriesRepository = FetchCategoriesRepository(expenseCategoriesDataSource, localExpenseCategoriesDataSource)

        val incomeCategoriesDataSource = FetchCategoriesDataSource(sheetsService, AppConstants.RANGE_INCOME_CATEGORIES)
        val localIncomeCategoriesDataSource = LocalCategoriesDataSource(this, AppConstants.KEY_INCOME_CATEGORIES_MAP)
        val incomeCategoriesRepository = FetchCategoriesRepository(incomeCategoriesDataSource, localIncomeCategoriesDataSource)

        val accountsDataSource = FetchAccountsDataSource(sheetsService)
        val localAccountsDataSource = LocalAccountsDataSource(this)
        val accountsRepository = FetchAccountsRepository(accountsDataSource, localAccountsDataSource)
        
        val searchDataSource = SearchTransactionsDataSource(sheetsService)
        val searchRepository = SearchTransactionsRepository(searchDataSource)

        val accountBalanceRepository = FetchAccountBalanceRepository(fetchDataSource)

        transactionVM = TransactionViewModel(
            fetchRepository,
            addRepository,
            deleteRepository,
            updateRepository,
            accountBalanceRepository,
        )
        categoryVM = CategoryViewModel(expenseCategoriesRepository, incomeCategoriesRepository)
        accountVM = AccountViewModel(accountsRepository)
        searchVM = SearchViewModel(searchRepository)
    }
}
