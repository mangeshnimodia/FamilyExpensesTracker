package com.familyexpensetracker.ui.screens

import com.familyexpensetracker.data.model.DateRange
import java.util.Calendar

class PeriodNavigator(private val calendarProvider: () -> Calendar = { Calendar.getInstance() }) {

    fun previous(current: DateRange): DateRange = when (current) {
        is DateRange.Day -> {
            val cal = calendarProvider().apply {
                time = current.date
                add(Calendar.DAY_OF_MONTH, -1)
            }
            DateRange.Day(cal.time)
        }
        is DateRange.Month -> {
            val (year, month) = if (current.month == Calendar.JANUARY) {
                Pair(current.year - 1, Calendar.DECEMBER)
            } else {
                Pair(current.year, current.month - 1)
            }
            DateRange.Month(year, month)
        }
        is DateRange.FinancialYear -> DateRange.FinancialYear(current.startYear - 1)
        is DateRange.All, is DateRange.Custom -> current
    }

    fun next(current: DateRange): DateRange = when (current) {
        is DateRange.Day -> {
            val cal = calendarProvider().apply {
                time = current.date
                add(Calendar.DAY_OF_MONTH, 1)
            }
            DateRange.Day(cal.time)
        }
        is DateRange.Month -> {
            val (year, month) = if (current.month == Calendar.DECEMBER) {
                Pair(current.year + 1, Calendar.JANUARY)
            } else {
                Pair(current.year, current.month + 1)
            }
            DateRange.Month(year, month)
        }
        is DateRange.FinancialYear -> DateRange.FinancialYear(current.startYear + 1)
        is DateRange.All, is DateRange.Custom -> current
    }

    fun isNavigable(current: DateRange): Boolean = when (current) {
        is DateRange.All, is DateRange.Custom -> false
        else -> true
    }

    fun defaultRangeFor(tab: PeriodTab): DateRange {
        val now = calendarProvider()
        return when (tab) {
            PeriodTab.Daily   -> DateRange.Day(now.time)
            PeriodTab.Monthly -> DateRange.Month(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
            PeriodTab.Yearly  -> {
                val fyStart = if (now.get(Calendar.MONTH) >= Calendar.APRIL) now.get(Calendar.YEAR) else now.get(Calendar.YEAR) - 1
                DateRange.FinancialYear(fyStart)
            }
            PeriodTab.All -> DateRange.All
        }
    }
}
