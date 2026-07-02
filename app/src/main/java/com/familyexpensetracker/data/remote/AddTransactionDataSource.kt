package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddTransactionDataSource(private val service: Sheets) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID
    private val range = AppConstants.RANGE_TRANSACTIONS

    suspend fun add(vararg transactions: Transaction): AppendValuesResponse = withContext(Dispatchers.IO) {
        val values = transactions.map { transaction ->
            listOf(
                transaction.txnId,
                transaction.date,
                transaction.amount.toString(),
                transaction.category,
                transaction.subcategory,
                transaction.paymentMethod,
                transaction.description,
                transaction.account,
                transaction.transferId ?: "",
            )
        }
        val body = ValueRange().setValues(values)
        service.spreadsheets().values()
            .append(spreadsheetId, range, body)
            .setValueInputOption(AppConstants.SHEETS_VALUE_INPUT_OPTION)
            .execute()
    }
}
