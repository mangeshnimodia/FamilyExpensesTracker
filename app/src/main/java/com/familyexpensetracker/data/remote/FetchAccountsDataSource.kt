package com.familyexpensetracker.data.remote

import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchAccountsDataSource(private val service: Sheets) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID
    private val range = AppConstants.RANGE_ACCOUNTS

    suspend fun fetch(): List<String> = withContext(Dispatchers.IO) {
        val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
            .execute()
        
        val values = response.getValues() ?: return@withContext emptyList()
        
        // Skip header if exists
        val dataRows = if (values.isNotEmpty() && (values[0].getOrNull(AppConstants.COL_ADMIN_ACCOUNT)?.toString()?.lowercase() == "account")) {
            values.drop(1)
        } else {
            values
        }

        dataRows.asSequence()
            .mapNotNull { it.getOrNull(AppConstants.COL_ADMIN_ACCOUNT)?.toString() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
    }
}
