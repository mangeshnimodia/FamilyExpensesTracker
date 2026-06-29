package com.familyexpensetracker.ui.components

import com.familyexpensetracker.data.model.DateRange
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class DateRangeFormatterTest {
    private val formatter = DateRangeFormatter()

    @Test
    fun testMonthFormatRolloverBug() {
        // Create a calendar set to Jan 31st
        val jan31 = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 31)
        }
        
        // Inject it into the formatter
        val bugFormatter = DateRangeFormatter(calendarProvider = { jan31 })

        val range = DateRange.Month(2024, Calendar.FEBRUARY)
        val result = bugFormatter.format(range)
        
        // With the bug, Jan 31st + set(Month, Feb) = March 2nd (or Feb 29/Mar 1)
        // because set(MONTH) doesn't adjust the day of month until it's too late.
        assertEquals("February 2024", result)
    }

    @Test
    fun testDayFormat() {
        val date = Calendar.getInstance().apply {
            set(2026, Calendar.JUNE, 28)
        }.time
        val range = DateRange.Day(date)
        // Expected format depends on locale, but typically "Sun, 28 Jun 2026"
        // Let's check against a known pattern if we can, or just trust SimpleDateFormat
        val result = formatter.format(range)
        assertTrue(result.contains("28"))
        assertTrue(result.contains("Jun"))
        assertTrue(result.contains("2026"))
    }

    @Test
    fun testMonthFormat() {
        val range = DateRange.Month(2026, Calendar.JUNE)
        val result = formatter.format(range)
        assertEquals("June 2026", result)
    }

    @Test
    fun testFinancialYearFormat() {
        val range = DateRange.FinancialYear(2026)
        val result = formatter.format(range)
        assertEquals("FY26-27", result)
        
        val range2 = DateRange.FinancialYear(2005)
        val result2 = formatter.format(range2)
        assertEquals("FY05-06", result2)
    }

    @Test
    fun testCustomFormat() {
        val start = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 1) }.time
        val end = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 10) }.time
        val range = DateRange.Custom(start, end)
        val result = formatter.format(range)
        assertTrue(result.contains("01 Jun 2026"))
        assertTrue(result.contains("10 Jun 2026"))
    }
    
    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
