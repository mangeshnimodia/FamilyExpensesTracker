package com.familyexpensetracker.data.remote

import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTransactionDataSource(private val service: Sheets) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID

    suspend fun delete(txnId: String) = withContext(Dispatchers.IO) {
        // 1. Find the row index
        val response = service.spreadsheets().values()[spreadsheetId, AppConstants.RANGE_TXN_IDS]
            .execute()
        val values = response.getValues() ?: return@withContext
        
        var rowIndex = -1
        for (i in values.indices) {
            if (values[i].getOrNull(0)?.toString() == txnId) {
                rowIndex = i
                break
            }
        }

        if (rowIndex != -1) {
            // 2. Get the sheetId for "Transactions"
            val spreadsheet = service.spreadsheets()[spreadsheetId].execute()
            val sheetId = spreadsheet.sheets.find { it.properties.title == AppConstants.SHEET_NAME_TRANSACTIONS }?.properties?.sheetId
                ?: throw Exception("Sheet '${AppConstants.SHEET_NAME_TRANSACTIONS}' not found")

            // 3. Delete the row
            val deleteRequest = Request().setDeleteDimension(
                DeleteDimensionRequest().setRange(
                    DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setStartIndex(rowIndex)
                        .setEndIndex(rowIndex + 1),
                )
            )
            
            val batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(listOf(deleteRequest))
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdate).execute()
        }
    }
}
