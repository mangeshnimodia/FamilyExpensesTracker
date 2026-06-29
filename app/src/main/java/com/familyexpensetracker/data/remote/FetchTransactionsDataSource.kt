package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.DateRangeFilter
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FetchTransactionsDataSource(private val service: Sheets) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID
    private val range = AppConstants.RANGE_TRANSACTIONS
    private val dateRangeFilter = DateRangeFilter()
    private val dateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault())

    suspend fun fetch(
        dateRange: DateRange? = null,
        filter: TransactionFilter? = null
    ): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Log.d("FetchDataSource", "Fetching from Spreadsheet: $spreadsheetId, Range: $range")
            val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
                .execute()
            
            val values = response.getValues() ?: return@withContext emptyList<Transaction>()
            Log.d("FetchDataSource", "Fetched ${values.size} rows (including header)")
            
            val filteredRows = filterRows(values, dateRange, filter)
            convertToTransactions(filteredRows)
        } catch (e: Exception) {
            Log.e("FetchDataSource", "Error fetching transactions: ${e.message}", e)
            throw e
        }
    }

    private fun filterRows(
        values: List<List<Any>>,
        dateRange: DateRange?,
        filter: TransactionFilter?
    ): Sequence<List<Any>> {
        return values.asSequence().drop(1).filter { row ->
            // 1. Date range filter
            if (!matchesDateRange(row, dateRange)) return@filter false

            // 2. Account filter
            if (!matchesAccountFilter(row, filter)) return@filter false

            // 3. Account type (income/expense)
            if (!matchesTypeFilter(row, filter)) return@filter false

            true
        }
    }

    private fun matchesDateRange(row: List<Any>, dateRange: DateRange?): Boolean {
        if (dateRange == null) return true
        val dateStr = row.getOrNull(AppConstants.COL_DATE)?.toString() ?: ""
        return try {
            val txnDate = dateFormat.parse(dateStr) ?: return false
            dateRangeFilter.isDateInRange(txnDate, dateRange)
        } catch (_: Exception) {
            false
        }
    }

    private fun matchesAccountFilter(row: List<Any>, filter: TransactionFilter?): Boolean {
        val rowAccount = row.getOrNull(AppConstants.COL_ACCOUNT)?.toString() ?: ""
        return filter?.selectedAccounts?.let { accounts ->
            accounts.isEmpty() || accounts.contains(rowAccount)
        } ?: true
    }

    private fun matchesTypeFilter(row: List<Any>, filter: TransactionFilter?): Boolean {
        val rowAmount = row.getOrNull(AppConstants.COL_AMOUNT)?.toString()?.toDoubleOrNull() ?: 0.0
        return filter?.type?.let { type ->
            when (type) {
                FilterType.INCOME -> rowAmount > 0
                FilterType.EXPENSE -> rowAmount < 0
                FilterType.BALANCE -> true
            }
        } ?: true
    }

    private fun convertToTransactions(rows: Sequence<List<Any>>): List<Transaction> {
        return rows.map { row ->
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
        }.toList()
    }
}
