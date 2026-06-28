package com.familyexpensetracker.data.remote

import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FetchCategoriesDataSource(
    private val service: Sheets,
    private val range: String,
) {
    private val spreadsheetId = AppConstants.SPREADSHEET_ID

    suspend fun fetch(): Map<String, List<String>> = withContext(Dispatchers.IO) {
        val response: ValueRange = service.spreadsheets().values()[spreadsheetId, range]
            .execute()
        
        val values = response.getValues() ?: return@withContext emptyMap()
        
        // Skip header if exists. Headers could be "Category", "ExpenseCategories", or "IncomeCategories"
        val dataRows = if (values.isNotEmpty() && (values[0].getOrNull(0)?.toString()?.lowercase()?.contains("category") == true)) {
            values.drop(1)
        } else {
            values
        }

        val categoriesMap = mutableMapOf<String, MutableList<String>>()
        
        dataRows.forEach { row ->
            val category = row.getOrNull(0)?.toString() ?: ""
            val subcategory = row.getOrNull(1)?.toString() ?: ""
            
            if (category.isNotBlank()) {
                categoriesMap.getOrPut(category) { mutableListOf() }.apply {
                    if (subcategory.isNotBlank()) add(subcategory)
                }
            }
        }
        
        categoriesMap.mapValues { it.value.distinct() }
    }
}
