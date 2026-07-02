package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.repository.FetchAccountsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val accountsRepository: FetchAccountsRepository,
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<String>>(emptyList())
    val accounts: StateFlow<List<String>> = _accounts.asStateFlow()

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                _accounts.value = accountsRepository.fetch()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
