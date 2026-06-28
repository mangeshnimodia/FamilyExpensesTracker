package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource

class FetchTransactionsRepository(private val dataSource: FetchTransactionsDataSource) {
    suspend fun fetch(dateRange: DateRange? = null): List<Transaction> = dataSource.fetch(dateRange)
}
