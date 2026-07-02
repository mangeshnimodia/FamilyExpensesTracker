package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val expenseCategoriesRepository: FetchCategoriesRepository,
    private val incomeCategoriesRepository: FetchCategoriesRepository,
) : ViewModel() {

    private val _expenseCategories = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val expenseCategories: StateFlow<Map<String, List<String>>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val incomeCategories: StateFlow<Map<String, List<String>>> = _incomeCategories.asStateFlow()

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
}
