package com.familyexpensetracker

import android.content.Context
import com.familyexpensetracker.data.local.LocalAccountsDataSource
import com.familyexpensetracker.data.local.LocalCategoriesDataSource
import com.familyexpensetracker.data.local.LocalPaymentMethodsDataSource
import com.familyexpensetracker.data.remote.AddTransactionDataSource
import com.familyexpensetracker.data.remote.DeleteTransactionDataSource
import com.familyexpensetracker.data.remote.FetchAccountsDataSource
import com.familyexpensetracker.data.remote.FetchCategoriesDataSource
import com.familyexpensetracker.data.remote.FetchPaymentMethodsDataSource
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource
import com.familyexpensetracker.data.remote.SearchTransactionsDataSource
import com.familyexpensetracker.data.remote.UpdateTransactionDataSource
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountBalanceRepository
import com.familyexpensetracker.data.repository.FetchAccountsRepository
import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import com.familyexpensetracker.data.repository.FetchPaymentMethodsRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import com.familyexpensetracker.data.repository.SearchTransactionsRepository
import com.familyexpensetracker.data.repository.UpdateTransactionRepository
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.PaymentMethodViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets

class AppDependencies(context: Context, sheetsService: Sheets) {
    val transactionViewModel: TransactionViewModel
    val categoryViewModel: CategoryViewModel
    val accountViewModel: AccountViewModel
    val searchViewModel: SearchViewModel
    val paymentMethodViewModel: PaymentMethodViewModel

    init {
        val fetchDataSource = FetchTransactionsDataSource(sheetsService)
        val fetchRepository = FetchTransactionsRepository(fetchDataSource)

        val addRepository = AddTransactionRepository(AddTransactionDataSource(sheetsService))
        val deleteRepository = DeleteTransactionRepository(DeleteTransactionDataSource(sheetsService))
        val updateRepository = UpdateTransactionRepository(UpdateTransactionDataSource(sheetsService))

        val expenseCategoriesRepository = FetchCategoriesRepository(
            FetchCategoriesDataSource(sheetsService, AppConstants.RANGE_EXPENSE_CATEGORIES),
            LocalCategoriesDataSource(context, AppConstants.KEY_EXPENSE_CATEGORIES_MAP),
        )
        val incomeCategoriesRepository = FetchCategoriesRepository(
            FetchCategoriesDataSource(sheetsService, AppConstants.RANGE_INCOME_CATEGORIES),
            LocalCategoriesDataSource(context, AppConstants.KEY_INCOME_CATEGORIES_MAP),
        )

        val accountsRepository = FetchAccountsRepository(
            FetchAccountsDataSource(sheetsService),
            LocalAccountsDataSource(context),
        )

        val searchRepository = SearchTransactionsRepository(SearchTransactionsDataSource(sheetsService))
        val accountBalanceRepository = FetchAccountBalanceRepository(fetchDataSource)

        val paymentMethodsRepository = FetchPaymentMethodsRepository(
            FetchPaymentMethodsDataSource(sheetsService),
            LocalPaymentMethodsDataSource(context),
        )

        transactionViewModel = TransactionViewModel(
            fetchRepository, addRepository, deleteRepository, updateRepository, accountBalanceRepository,
        )
        categoryViewModel = CategoryViewModel(expenseCategoriesRepository, incomeCategoriesRepository)
        accountViewModel = AccountViewModel(accountsRepository)
        searchViewModel = SearchViewModel(searchRepository)
        paymentMethodViewModel = PaymentMethodViewModel(paymentMethodsRepository)
    }
}
