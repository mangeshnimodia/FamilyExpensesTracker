package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchTransactionsDataSource(private val service: Sheets) {

    private val spreadsheetId = AppConstants.SPREADSHEET_ID

    suspend fun search(query: String): List<Transaction> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        Log.d("SearchDataSource", "Searching for: $query")
        val response = service.spreadsheets().values()[spreadsheetId, AppConstants.RANGE_TRANSACTIONS].execute()
        val values = response.getValues() ?: return@withContext emptyList()

        val lowerQuery = query.lowercase()
        values.asSequence()
            .drop(1) // skip header
            .filter { row ->
                val description = row.getOrNull(AppConstants.COL_DESCRIPTION)?.toString() ?: ""
                description.lowercase().contains(lowerQuery)
            }
            .map { row ->
                Transaction(
                    txnId = row.getOrNull(AppConstants.COL_TXN_ID)?.toString() ?: "",
                    date = row.getOrNull(AppConstants.COL_DATE)?.toString() ?: "",
                    amount = row.getOrNull(AppConstants.COL_AMOUNT)?.toString()?.toDoubleOrNull() ?: 0.0,
                    category = row.getOrNull(AppConstants.COL_CATEGORY)?.toString() ?: "",
                    subcategory = row.getOrNull(AppConstants.COL_SUBCATEGORY)?.toString() ?: "",
                    paymentMethod = row.getOrNull(AppConstants.COL_PAYMENT_METHOD)?.toString() ?: "",
                    description = row.getOrNull(AppConstants.COL_DESCRIPTION)?.toString() ?: "",
                    account = row.getOrNull(AppConstants.COL_ACCOUNT)?.toString() ?: "",
                    transferId = row.getOrNull(AppConstants.COL_TRANSFER_ID)?.toString(),
                )
            }
            .toList()
    }
}
