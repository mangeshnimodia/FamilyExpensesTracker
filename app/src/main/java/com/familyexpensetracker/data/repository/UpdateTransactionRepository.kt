package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.UpdateTransactionDataSource

class UpdateTransactionRepository(private val dataSource: UpdateTransactionDataSource) {
    suspend fun update(transaction: Transaction) {
        dataSource.update(transaction)
    }
}
