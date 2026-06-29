package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateTransactionDataSource(private val service: Sheets) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID

    suspend fun update(transaction: Transaction) = withContext(Dispatchers.IO) {
        // 1. Find the row index
        val response = service.spreadsheets().values()[spreadsheetId, AppConstants.RANGE_TXN_IDS]
            .execute()
        val values = response.getValues() ?: return@withContext
        
        var rowIndex = -1
        for (i in values.indices) {
            if (values[i].getOrNull(0)?.toString() == transaction.txnId) {
                rowIndex = i
                break
            }
        }

        if (rowIndex != -1) {
            // Google Sheets row indices are 1-based, but we are using range notation
            // Row index in 'values' is 0-based. 
            // In A:I, rowIndex 0 is row 1, rowIndex 1 is row 2, etc.
            val sheetRowIndex = rowIndex + 1
            val range = "${AppConstants.SHEET_NAME_TRANSACTIONS}!A$sheetRowIndex:I$sheetRowIndex"
            
            val updateValues = listOf(
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
                ),
            )
            val body = ValueRange().setValues(updateValues)
            
            service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()
        }
    }
}
