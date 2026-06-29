package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.DateRangeFilter
import com.familyexpensetracker.data.model.Transaction
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

    suspend fun fetch(dateRange: DateRange? = null): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            Log.d("FetchDataSource", "Fetching from Spreadsheet: $spreadsheetId, Range: $range")
            val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
                .execute()
            
            val values = response.getValues() ?: return@withContext emptyList<Transaction>()
            Log.d("FetchDataSource", "Fetched ${values.size} rows (including header)")
            
            val dateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault())
            
            val transactions = values.asSequence().drop(1).map { row ->
                Transaction(
                    txnId = row.getOrNull(0)?.toString() ?: "",
                    date = row.getOrNull(1)?.toString() ?: "",
                    amount = row.getOrNull(2)?.toString()?.toDoubleOrNull() ?: 0.0,
                    category = row.getOrNull(3)?.toString() ?: "",
                    subcategory = row.getOrNull(4)?.toString() ?: "",
                    paymentMethod = row.getOrNull(5)?.toString() ?: "",
                    description = row.getOrNull(6)?.toString() ?: "",
                    account = row.getOrNull(7)?.toString() ?: "",
                    transferId = row.getOrNull(8)?.toString(),
                )
            }.toList()

            if (dateRange != null) {
                val filtered = transactions.filter { txn ->
                    try {
                        val txnDate = dateFormat.parse(txn.date) ?: return@filter false
                        dateRangeFilter.isDateInRange(txnDate, dateRange)
                    } catch (_: Exception) {
                        false
                    }
                }
                Log.d("FetchDataSource", "Filtered to ${filtered.size} transactions for range $dateRange")
                filtered
            } else {
                transactions
            }
        } catch (e: Exception) {
            Log.e("FetchDataSource", "Error fetching transactions: ${e.message}", e)
            throw e
        }
    }
}
