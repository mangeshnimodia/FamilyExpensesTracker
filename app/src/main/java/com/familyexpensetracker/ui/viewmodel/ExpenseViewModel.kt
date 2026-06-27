package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun fetchTransactions() {
        viewModelScope.launch {
            performFetch()
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.addTransaction(transaction)
                performFetch() // Wait for the refresh to finish before hiding loader
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Add failed: ${e.message}"
                _isLoading.value = false // Hide loader only on error, otherwise performFetch hides it
            }
        }
    }

    private suspend fun performFetch() {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val calendar = Calendar.getInstance()
            calendar.set(2026, Calendar.JUNE, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            _transactions.value = repository.getTransactions(startDate)
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
