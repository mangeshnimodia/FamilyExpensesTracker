package com.familyexpensetracker.data.model

import java.util.Date

sealed class DateRange {
    data class Day(val date: Date) : DateRange()
    data class Month(val year: Int, val month: Int) : DateRange() // month is 0-indexed
    data class FinancialYear(val startYear: Int) : DateRange() // e.g., 2024 for FY24-25
    data class Custom(val startDate: Date, val endDate: Date) : DateRange()
}
