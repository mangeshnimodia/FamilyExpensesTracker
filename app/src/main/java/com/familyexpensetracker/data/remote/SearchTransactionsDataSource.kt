package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchTransactionsDataSource(private val service: Sheets) {

    private val spreadsheetId = AppConstants.SPREADSHEET_ID
    private val rowMapper = TransactionRowMapper()

    suspend fun search(query: String): List<Transaction> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        Log.d(TAG, "Searching for: $query")
        val response = service.spreadsheets().values()[spreadsheetId, AppConstants.RANGE_TRANSACTIONS].execute()
        val values = response.getValues() ?: return@withContext emptyList()

        val lowerQuery = query.lowercase()
        values.asSequence()
            .drop(1) // skip header
            .filter { row ->
                val description = row.getOrNull(AppConstants.COL_DESCRIPTION)?.toString() ?: ""
                description.lowercase().contains(lowerQuery)
            }
            .map { rowMapper.mapRow(it) }
            .toList()
    }

    companion object {
        private val TAG = SearchTransactionsDataSource::class.simpleName!!
    }
}
