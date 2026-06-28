package com.familyexpensetracker.data.remote

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

    suspend fun fetch(startDate: Date? = null): List<Transaction> = withContext(Dispatchers.IO) {
        val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
            .execute()
        
        val values = response.getValues() ?: return@withContext emptyList<Transaction>()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        
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

        if (startDate != null) {
            val startCalendar = Calendar.getInstance().apply { time = startDate }
            transactions.filter { txn ->
                try {
                    val txnDate = dateFormat.parse(txn.date)
                    if (txnDate == null) return@filter false
                    val txnCalendar = Calendar.getInstance().apply { time = txnDate }
                    
                    txnCalendar.get(Calendar.YEAR) == startCalendar.get(Calendar.YEAR) &&
                    txnCalendar.get(Calendar.MONTH) == startCalendar.get(Calendar.MONTH) &&
                    txnCalendar.get(Calendar.DAY_OF_MONTH) == startCalendar.get(Calendar.DAY_OF_MONTH)
                } catch (_: Exception) {
                    false
                }
            }
        } else {
            transactions
        }
    }
}
