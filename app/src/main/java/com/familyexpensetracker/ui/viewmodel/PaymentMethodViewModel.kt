package com.familyexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.familyexpensetracker.data.repository.FetchPaymentMethodsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentMethodViewModel(
    private val paymentMethodsRepository: FetchPaymentMethodsRepository,
) : ViewModel() {

    private val _paymentMethods = MutableStateFlow<List<String>>(emptyList())
    val paymentMethods: StateFlow<List<String>> = _paymentMethods.asStateFlow()

    fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _paymentMethods.value = paymentMethodsRepository.fetch()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
