package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.AddTransactionDataSource

class AddTransactionRepository(private val dataSource: AddTransactionDataSource) {
    suspend fun add(vararg transactions: Transaction) = dataSource.add(*transactions)
}
