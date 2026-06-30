package com.familyexpensetracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class MonthSummaryTest {

    @Test
    fun `MonthSummary holds expected values`() {
        val summary = MonthSummary(
            year = 2026,
            month = Calendar.JUNE,
            totalAmount = -5000.0,
            transactionCount = 12
        )
        assertEquals(2026, summary.year)
        assertEquals(Calendar.JUNE, summary.month)
        assertEquals(-5000.0, summary.totalAmount, 0.001)
        assertEquals(12, summary.transactionCount)
    }

    @Test
    fun `MonthSummary month is zero-indexed`() {
        val jan = MonthSummary(2026, Calendar.JANUARY, 0.0, 0)
        val dec = MonthSummary(2026, Calendar.DECEMBER, 0.0, 0)
        assertEquals(0, jan.month)
        assertEquals(11, dec.month)
    }

    @Test
    fun `MonthSummary equality based on data`() {
        val a = MonthSummary(2026, Calendar.JUNE, -5000.0, 12)
        val b = MonthSummary(2026, Calendar.JUNE, -5000.0, 12)
        assertEquals(a, b)
    }

    @Test
    fun `MonthSummary copy works correctly`() {
        val original = MonthSummary(2026, Calendar.JUNE, -5000.0, 12)
        val copied = original.copy(totalAmount = -6000.0)
        assertEquals(-6000.0, copied.totalAmount, 0.001)
        assertEquals(2026, copied.year)
    }
}
