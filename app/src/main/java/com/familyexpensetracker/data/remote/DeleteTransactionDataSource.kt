package com.familyexpensetracker.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTransactionDataSource(private val service: Sheets) {
    private val spreadsheetId = "17Vf1XbIATnDRslj5BoNJFlDyq7SFINnyiAhX-Sx1FWU"

    suspend fun delete(txnId: String) = withContext(Dispatchers.IO) {
        // 1. Find the row index
        val response = service.spreadsheets().values()[spreadsheetId, "Transactions!A:A"]
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
            val spreadsheet = service.spreadsheets().get(spreadsheetId).execute()
            val sheetId = spreadsheet.sheets.find { it.properties.title == "Transactions" }?.properties?.sheetId
                ?: throw Exception("Sheet 'Transactions' not found")

            // 3. Delete the row
            val deleteRequest = Request().setDeleteDimension(
                DeleteDimensionRequest().setRange(
                    DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setStartIndex(rowIndex)
                        .setEndIndex(rowIndex + 1)
                )
            )
            
            val batchUpdate = BatchUpdateSpreadsheetRequest().setRequests(listOf(deleteRequest))
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdate).execute()
        }
    }
}
