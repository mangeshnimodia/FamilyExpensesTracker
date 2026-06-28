package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val categoriesRepository: FetchCategoriesRepository,
    private val accountsRepository: FetchAccountsRepository,
) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _categories = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val categories: StateFlow<Map<String, List<String>>> = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<String>>(emptyList())
    val accounts: StateFlow<List<String>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate: StateFlow<Date?> = _selectedDate.asStateFlow()

    fun fetchTransactions(date: Date) {
        _selectedDate.value = date
        viewModelScope.launch {
            performFetch(date)
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = categoriesRepository.fetch()
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
                _selectedDate.value = transactionDate
                performFetch(transactionDate)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Add failed: ${e.message}"
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
                _selectedDate.value?.let { performFetch(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Delete failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun performFetch(date: Date) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            // Set time to midnight for consistent filtering
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val midnightDate = calendar.time

            _transactions.value = fetchRepository.fetch(midnightDate)
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
