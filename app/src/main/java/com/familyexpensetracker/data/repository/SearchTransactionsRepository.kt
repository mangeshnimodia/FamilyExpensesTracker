package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.SearchTransactionsDataSource

class SearchTransactionsRepository(private val dataSource: SearchTransactionsDataSource) {
    suspend fun search(query: String): List<Transaction> = dataSource.search(query)
}
