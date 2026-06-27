package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.GoogleSheetsDataSource
import java.util.Date

class ExpenseRepository(private val googleSheetsDataSource: GoogleSheetsDataSource) {
    suspend fun getTransactions(startDate: Date? = null) = googleSheetsDataSource.getTransactions(startDate)
    suspend fun addTransaction(transaction: Transaction) = googleSheetsDataSource.addTransaction(transaction)
    suspend fun deleteTransaction(txnId: String) = googleSheetsDataSource.deleteTransaction(txnId)
}
