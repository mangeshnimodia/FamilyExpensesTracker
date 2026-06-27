package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.GoogleSheetsHelper

import java.util.Date

class ExpenseRepository(private val googleSheetsHelper: GoogleSheetsHelper) {
    suspend fun getTransactions(startDate: Date? = null) = googleSheetsHelper.getTransactions(startDate)
    suspend fun addTransaction(transaction: Transaction) = googleSheetsHelper.addTransaction(transaction)
}
