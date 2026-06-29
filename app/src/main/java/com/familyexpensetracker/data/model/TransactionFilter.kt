package com.familyexpensetracker.data.model

data class TransactionFilter(
    val selectedAccounts: List<String> = emptyList(),
    val type: FilterType = FilterType.BALANCE
)

enum class FilterType {
    INCOME,
    EXPENSE,
    BALANCE
}
