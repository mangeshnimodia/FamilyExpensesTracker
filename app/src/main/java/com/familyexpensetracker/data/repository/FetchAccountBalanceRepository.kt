package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource

class FetchAccountBalanceRepository(private val dataSource: FetchTransactionsDataSource) {
    suspend fun fetchBalance(account: String?): Double {
        val filter = TransactionFilter(
            type = FilterType.BALANCE,
            selectedAccounts = if (account != null) listOf(account) else emptyList(),
        )
        return dataSource.fetch(DateRange.All, filter).sumOf { it.amount }
    }
}
