package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.repository.SearchTransactionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchTransactionsRepository,
) : ViewModel() {

    private var searchJob: Job? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Transaction>>(emptyList())
    val searchResults: StateFlow<List<Transaction>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter())
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    fun updateSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun searchTransactions(query: String, filter: SearchFilter = _searchFilter.value) {
        searchJob?.cancel()
        if (query.isBlank() && filter.isEmpty()) {
            _searchQuery.value = ""
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        _searchQuery.value = query
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = searchRepository.search(query, filter)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }
}
