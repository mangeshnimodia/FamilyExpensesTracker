package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.SearchTransactionsDataSource

class SearchTransactionsRepository(private val dataSource: SearchTransactionsDataSource) {
    suspend fun search(query: String, filter: SearchFilter = SearchFilter()): List<Transaction> =
        dataSource.search(query, filter)
}
