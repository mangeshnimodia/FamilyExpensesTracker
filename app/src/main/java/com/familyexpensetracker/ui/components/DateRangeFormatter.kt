package com.familyexpensetracker.ui.components

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.*

class DateRangeFormatter(private val calendarProvider: () -> Calendar = { Calendar.getInstance() }) {
    private val dayFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_UI, Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun format(dateRange: DateRange): String {
        return when (dateRange) {
            is DateRange.Day -> dayFormat.format(dateRange.date)
            is DateRange.Month -> {
                val cal = calendarProvider().apply {
                    clear()
                    set(Calendar.YEAR, dateRange.year)
                    set(Calendar.MONTH, dateRange.month)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                monthFormat.format(cal.time)
            }
            is DateRange.FinancialYear -> {
                val startYearShort = dateRange.startYear % 100
                val endYearShort = (dateRange.startYear + 1) % 100
                "FY%02d-%02d".format(startYearShort, endYearShort)
            }
            is DateRange.Custom -> {
                "${dayFormat.format(dateRange.startDate)} - ${dayFormat.format(dateRange.endDate)}"
            }
            is DateRange.All -> "All Time"
        }
    }
}
