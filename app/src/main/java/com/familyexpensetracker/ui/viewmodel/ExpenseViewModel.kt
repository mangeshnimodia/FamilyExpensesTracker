package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.ui.screens.PeriodTab
import com.familyexpensetracker.ui.screens.TransactionSummaryCalculator
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.UpdateTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountsRepository
import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import com.familyexpensetracker.data.repository.SearchTransactionsRepository
import com.familyexpensetracker.utils.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(
    private val fetchRepository: FetchTransactionsRepository,
    private val addRepository: AddTransactionRepository,
    private val deleteRepository: DeleteTransactionRepository,
    private val updateRepository: UpdateTransactionRepository,
    private val expenseCategoriesRepository: FetchCategoriesRepository,
    private val incomeCategoriesRepository: FetchCategoriesRepository,
    private val accountsRepository: FetchAccountsRepository,
    private val searchRepository: SearchTransactionsRepository,
    private val summaryCalculator: TransactionSummaryCalculator = TransactionSummaryCalculator(),
) : ViewModel() {
    private var fetchJob: Job? = null
    private var searchJob: Job? = null

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _expenseCategories = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val expenseCategories: StateFlow<Map<String, List<String>>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val incomeCategories: StateFlow<Map<String, List<String>>> = _incomeCategories.asStateFlow()

    private val _accounts = MutableStateFlow<List<String>>(emptyList())
    val accounts: StateFlow<List<String>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<DateRange>(DateRange.Day(Date()))
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TransactionFilter(selectedAccounts = listOf(AppConstants.DEFAULT_ACCOUNT)))
    val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter.asStateFlow()

    private val _selectedPeriodTab = MutableStateFlow(PeriodTab.Daily)
    val selectedPeriodTab: StateFlow<PeriodTab> = _selectedPeriodTab.asStateFlow()

    private val _selectedAccount = MutableStateFlow<String?>(AppConstants.DEFAULT_ACCOUNT)
    val selectedAccount: StateFlow<String?> = _selectedAccount.asStateFlow()

    private val _categorySummaries = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categorySummaries: StateFlow<List<CategorySummary>> = _categorySummaries.asStateFlow()

    private val _monthSummaries = MutableStateFlow<List<MonthSummary>>(emptyList())
    val monthSummaries: StateFlow<List<MonthSummary>> = _monthSummaries.asStateFlow()

    private val _expenseTotal = MutableStateFlow(0.0)
    val expenseTotal: StateFlow<Double> = _expenseTotal.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Transaction>>(emptyList())
    val searchResults: StateFlow<List<Transaction>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun setSelectedDateRange(range: DateRange) {
        if (_selectedDateRange.value != range) {
            _selectedDateRange.value = range
            _transactions.value = emptyList() // Clear stale data for old range
        }
    }

    fun setSelectedFilter(filter: TransactionFilter) {
        if (_selectedFilter.value != filter) {
            _selectedFilter.value = filter
            _transactions.value = emptyList()
        }
    }

    fun setSelectedPeriodTab(tab: PeriodTab) {
        if (_selectedPeriodTab.value != tab) {
            _selectedPeriodTab.value = tab
        }
    }

    fun setSelectedAccount(account: String?) {
        if (_selectedAccount.value != account) {
            _selectedAccount.value = account
            _transactions.value = emptyList()
        }
    }

    fun fetchTransactions() {
        val range = _selectedDateRange.value
        val filter = _selectedFilter.value
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            performFetch(range, filter)
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _expenseCategories.value = expenseCategoriesRepository.fetch()
                _incomeCategories.value = incomeCategoriesRepository.fetch()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                _accounts.value = accountsRepository.fetch()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTransaction(transaction: Transaction, transactionDate: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                addRepository.add(transaction)
                setSelectedDateRange(DateRange.Day(transactionDate))
                _transactions.value = emptyList() // Clear list to force refresh
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Add failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAccountTransfer(
        fromAccount: String,
        toAccount: String,
        amount: Double,
        paymentMethod: String,
        description: String,
        transactionDate: Date
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val transferId = UUID.randomUUID().toString()
                val dateStr = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault()).format(transactionDate)

                val expenseEntry = Transaction(
                    txnId = UUID.randomUUID().toString(),
                    date = dateStr,
                    amount = -amount,
                    category = AppConstants.CATEGORY_ACCOUNT_TRANSFER,
                    subcategory = "",
                    paymentMethod = paymentMethod,
                    description = description,
                    account = fromAccount,
                    transferId = transferId
                )

                val incomeEntry = Transaction(
                    txnId = UUID.randomUUID().toString(),
                    date = dateStr,
                    amount = amount,
                    category = AppConstants.DEFAULT_INCOME_CATEGORY,
                    subcategory = AppConstants.SUBCATEGORY_ACCOUNT_TRANSFER,
                    paymentMethod = paymentMethod,
                    description = description,
                    account = toAccount,
                    transferId = transferId
                )

                addRepository.add(expenseEntry, incomeEntry)
                setSelectedDateRange(DateRange.Day(transactionDate))
                _transactions.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Transfer failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransaction(transaction: Transaction, transactionDate: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                updateRepository.update(transaction)
                setSelectedDateRange(DateRange.Day(transactionDate))
                _transactions.value = emptyList() // Clear list to force refresh
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Update failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(txnId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                deleteRepository.delete(txnId)
                _transactions.value = emptyList() // Clear list to force refresh
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Delete failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun performFetch(range: DateRange, filter: TransactionFilter) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val fetched = fetchRepository.fetch(range, filter)
            _transactions.value = fetched
            _expenseTotal.value = fetched.filter { it.amount < 0 }.sumOf { -it.amount }
            _categorySummaries.value = summaryCalculator.calculateCategorySummaries(fetched)
            _monthSummaries.value = when (range) {
                is DateRange.FinancialYear -> summaryCalculator.calculateFYMonthSummaries(fetched, range.startYear)
                else -> summaryCalculator.calculateMonthSummaries(fetched)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "Fetch failed: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun searchTransactions(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            _errorMessage.value = null
            try {
                _searchResults.value = searchRepository.search(query)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Search failed: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
