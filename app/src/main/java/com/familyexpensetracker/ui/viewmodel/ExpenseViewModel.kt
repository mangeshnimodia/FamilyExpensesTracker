package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountsRepository
import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(
    private val fetchRepository: FetchTransactionsRepository,
    private val addRepository: AddTransactionRepository,
    private val deleteRepository: DeleteTransactionRepository,
    private val expenseCategoriesRepository: FetchCategoriesRepository,
    private val incomeCategoriesRepository: FetchCategoriesRepository,
    private val accountsRepository: FetchAccountsRepository,
) : ViewModel() {
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

    fun setSelectedDateRange(range: DateRange) {
        if (_selectedDateRange.value != range) {
            _selectedDateRange.value = range
            _transactions.value = emptyList() // Clear stale data for old range
        }
    }

    fun fetchTransactions() {
        val range = _selectedDateRange.value
        viewModelScope.launch {
            performFetch(range)
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

    private suspend fun performFetch(range: DateRange) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            _transactions.value = fetchRepository.fetch(range)
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "Fetch failed: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
