package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.remote.DeleteTransactionDataSource

class DeleteTransactionRepository(private val dataSource: DeleteTransactionDataSource) {
    suspend fun delete(txnId: String) = dataSource.delete(txnId)
}
