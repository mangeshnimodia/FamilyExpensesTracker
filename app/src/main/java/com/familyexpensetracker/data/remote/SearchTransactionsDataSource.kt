package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class SearchTransactionsDataSource(private val service: Sheets) {

    private val spreadsheetId = AppConstants.SPREADSHEET_ID
    private val rowMapper = TransactionRowMapper()

    suspend fun search(query: String, filter: SearchFilter = SearchFilter()): List<Transaction> = withContext(Dispatchers.IO) {
        if (query.isBlank() && filter.isEmpty()) return@withContext emptyList()

        Log.d(TAG, "Searching for: $query with filter: $filter")
        val response = service.spreadsheets().values()[spreadsheetId, AppConstants.RANGE_TRANSACTIONS].execute()
        val values = response.getValues() ?: return@withContext emptyList()

        val lowerQuery = query.lowercase()
        values.asSequence()
            .drop(1) // skip header
            .filter { row ->
                matchesQuery(row, lowerQuery, filter.exactMatch) &&
                    matchesFilter(row, filter)
            }
            .map { rowMapper.mapRow(it) }
            .toList()
    }

    private fun matchesQuery(row: List<Any>, lowerQuery: String, exactMatch: Boolean): Boolean {
        if (lowerQuery.isBlank()) return true
        val description = row.getOrNull(AppConstants.COL_DESCRIPTION)?.toString()?.lowercase() ?: ""
        return if (exactMatch) description == lowerQuery else description.contains(lowerQuery)
    }

    private fun matchesFilter(row: List<Any>, filter: SearchFilter): Boolean {
        if (!filter.account.isBlank() &&
            row.getOrNull(AppConstants.COL_ACCOUNT)?.toString() != filter.account
        ) return false
        if (!filter.category.isBlank() &&
            row.getOrNull(AppConstants.COL_CATEGORY)?.toString() != filter.category
        ) return false
        if (!filter.subcategory.isBlank() &&
            row.getOrNull(AppConstants.COL_SUBCATEGORY)?.toString() != filter.subcategory
        ) return false
        if (!filter.paymentMethod.isBlank() &&
            row.getOrNull(AppConstants.COL_PAYMENT_METHOD)?.toString() != filter.paymentMethod
        ) return false
        val rawAmount = row.getOrNull(AppConstants.COL_AMOUNT)?.toString()?.toDoubleOrNull() ?: 0.0
        val absAmount = abs(rawAmount)
        if (filter.minAmount != null && absAmount < filter.minAmount) return false
        if (filter.maxAmount != null && absAmount > filter.maxAmount) return false
        return true
    }

    companion object {
        private val TAG = SearchTransactionsDataSource::class.simpleName!!
    }
}
