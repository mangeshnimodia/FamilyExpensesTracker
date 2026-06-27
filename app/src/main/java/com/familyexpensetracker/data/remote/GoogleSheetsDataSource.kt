package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsDataSource(private val service: Sheets) {
    init {
        android.util.Log.d("GoogleSheetsDataSource", "Initializing with service")
    }
    private val spreadsheetId = "17Vf1XbIATnDRslj5BoNJFlDyq7SFINnyiAhX-Sx1FWU"
    private val range = "Transactions!A:I"

    suspend fun getTransactions(startDate: Date? = null): List<Transaction> = withContext(Dispatchers.IO) {
        val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
            .execute()
        
        val values = response.getValues() ?: return@withContext emptyList<Transaction>()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        
        // Skip header row
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
            transactions.filter { txn ->
                try {
                    val txnDate = dateFormat.parse(txn.date)
                    (txnDate != null) && ((txnDate.after(startDate)) || (txnDate == startDate))
                } catch (_: Exception) {
                    false
                }
            }
        } else {
            transactions
        }
    }

    suspend fun addTransaction(transaction: Transaction): com.google.api.services.sheets.v4.model.AppendValuesResponse = withContext(Dispatchers.IO) {
        val values = listOf(
            listOf(
                transaction.txnId,
                transaction.date,
                transaction.amount.toString(), // Convert to string for Sheets
                transaction.category,
                transaction.subcategory,
                transaction.paymentMethod,
                transaction.description,
                transaction.account,
                transaction.transferId ?: "",
            ),
        )
        val body = ValueRange().setValues(values)
        service.spreadsheets().values()
            .append(spreadsheetId, range, body)
            .setValueInputOption("USER_ENTERED")
            .execute()
    }
}
