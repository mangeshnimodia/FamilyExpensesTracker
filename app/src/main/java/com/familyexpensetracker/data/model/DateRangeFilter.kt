package com.familyexpensetracker.data.model

import java.util.*

class DateRangeFilter {
    fun isDateInRange(date: Date, dateRange: DateRange): Boolean {
        val cal = Calendar.getInstance().apply { time = date }
        val txnYear = cal.get(Calendar.YEAR)
        val txnMonth = cal.get(Calendar.MONTH)
        val txnDay = cal.get(Calendar.DAY_OF_MONTH)

        return when (dateRange) {
            is DateRange.All -> true
            is DateRange.Day -> {
                val rangeCal = Calendar.getInstance().apply { time = dateRange.date }
                txnYear == rangeCal.get(Calendar.YEAR) &&
                        txnMonth == rangeCal.get(Calendar.MONTH) &&
                        txnDay == rangeCal.get(Calendar.DAY_OF_MONTH)
            }
            is DateRange.Month -> {
                txnYear == dateRange.year && txnMonth == dateRange.month
            }
            is DateRange.FinancialYear -> {
                // FY starts April 1st of startYear and ends March 31st of startYear + 1
                val startCal = Calendar.getInstance().apply {
                    set(dateRange.startYear, Calendar.APRIL, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(dateRange.startYear + 1, Calendar.MARCH, 31, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                !date.before(startCal.time) && !date.after(endCal.time)
            }
            is DateRange.Custom -> {
                val startCal = Calendar.getInstance().apply {
                    time = dateRange.startDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    time = dateRange.endDate
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                !date.before(startCal.time) && !date.after(endCal.time)
            }
        }
    }
}
