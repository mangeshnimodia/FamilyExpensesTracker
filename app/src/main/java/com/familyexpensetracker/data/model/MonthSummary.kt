package com.familyexpensetracker.data.model

data class MonthSummary(
    val year: Int,
    val month: Int, // 0-indexed, matching Calendar.MONTH
    val totalAmount: Double,
    val transactionCount: Int
)
