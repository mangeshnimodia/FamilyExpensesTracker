package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountBalanceRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import com.familyexpensetracker.data.repository.UpdateTransactionRepository
import com.familyexpensetracker.ui.screens.PeriodTab
import com.familyexpensetracker.ui.screens.TransactionSummaryCalculator
import com.familyexpensetracker.utils.AppConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TransactionViewModel(
    private val fetchRepository: FetchTransactionsRepository,
    private val addRepository: AddTransactionRepository,
    private val deleteRepository: DeleteTransactionRepository,
    private val updateRepository: UpdateTransactionRepository,
    private val accountBalanceRepository: FetchAccountBalanceRepository,
    private val summaryCalculator: TransactionSummaryCalculator = TransactionSummaryCalculator(),
) : ViewModel() {

    private var fetchJob: Job? = null

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
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

    private val _accountBalance = MutableStateFlow<Double?>(null)
    val accountBalance: StateFlow<Double?> = _accountBalance.asStateFlow()

    fun setSelectedDateRange(range: DateRange) {
        if (_selectedDateRange.value != range) {
            _selectedDateRange.value = range
            _transactions.value = emptyList()
            _expenseTotal.value = 0.0
        }
    }

    fun setSelectedFilter(filter: TransactionFilter) {
        if (_selectedFilter.value != filter) {
            _selectedFilter.value = filter
            _transactions.value = emptyList()
            _expenseTotal.value = 0.0
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
            _expenseTotal.value = 0.0
            _accountBalance.value = null
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

    fun fetchAccountBalance() {
        viewModelScope.launch {
            try {
                _accountBalance.value = accountBalanceRepository.fetchBalance(_selectedAccount.value)
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
                _transactions.value = emptyList()
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
        transactionDate: Date,
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
                    transferId = transferId,
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
                    transferId = transferId,
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
                _transactions.value = emptyList()
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
                _transactions.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Delete failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private suspend fun performFetch(range: DateRange, filter: TransactionFilter) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val fetched = fetchRepository.fetch(range, filter)
            _transactions.value = fetched
            _expenseTotal.value = fetched.filter { it.amount < 0 }.sumOf { it.amount }
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
}
