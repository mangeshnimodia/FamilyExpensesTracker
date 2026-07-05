package com.familyexpensetracker.utils

import java.text.SimpleDateFormat
import java.util.Locale

object TransactionDateFormatter {
    private val inputFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault())
    private val outputFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_TRANSACTION, Locale.getDefault())

    fun format(date: String): String {
        return try {
            val parsed = inputFormat.parse(date) ?: return date
            outputFormat.format(parsed)
        } catch (e: Exception) {
            date
        }
    }
}
