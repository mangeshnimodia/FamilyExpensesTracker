package com.familyexpensetracker.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class DateRangeFilterTest {
    private val filter = DateRangeFilter()

    @Test
    fun testDayRange() {
        val date = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 28)
        }.time
        val range = DateRange.Day(date)
        
        assertTrue(filter.isDateInRange(date, range))
        
        val otherDate = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 29)
        }.time
        assertFalse(filter.isDateInRange(otherDate, range))
    }

    @Test
    fun testMonthRange() {
        val range = DateRange.Month(2026, Calendar.JUNE)
        
        val dateInMonth = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 15)
        }.time
        assertTrue(filter.isDateInRange(dateInMonth, range))
        
        val dateOutsideMonth = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 1)
        }.time
        assertFalse(filter.isDateInRange(dateOutsideMonth, range))
    }

    @Test
    fun testFinancialYearRangeEdgeCases() {
        val range = DateRange.FinancialYear(2025) // FY25-26: Apr 2025 - Mar 2026
        
        // Last millisecond of the FY
        val lastDay = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        assertTrue(filter.isDateInRange(lastDay, range))

        // First millisecond after the FY
        val nextDay = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        assertFalse(filter.isDateInRange(nextDay, range))
    }

    @Test
    fun testCustomRange() {
        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 1)
        }.time
        val endDate = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 10)
        }.time
        val range = DateRange.Custom(startDate, endDate)
        
        val dateInRange = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 5)
        }.time
        assertTrue(filter.isDateInRange(dateInRange, range))
        
        val dateAtStart = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 1)
        }.time
        assertTrue(filter.isDateInRange(dateAtStart, range))
        
        val dateAtEnd = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 10)
        }.time
        assertTrue(filter.isDateInRange(dateAtEnd, range))
        
        val dateOutside = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 11)
        }.time
        assertFalse(filter.isDateInRange(dateOutside, range))
    }
}
