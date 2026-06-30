package com.familyexpensetracker.data.model

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Calendar

class DateRangeAllTest {

    private val filter = DateRangeFilter()

    @Test
    fun `DateRange All matches any date`() {
        val range = DateRange.All
        val dates = listOf(
            Calendar.getInstance().apply { set(2000, Calendar.JANUARY, 1) }.time,
            Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time,
            Calendar.getInstance().apply { set(2099, Calendar.DECEMBER, 31) }.time,
        )
        dates.forEach { date ->
            assertTrue("Expected All to match $date", filter.isDateInRange(date, range))
        }
    }

    @Test
    fun `DateRange All is a singleton`() {
        assertTrue(DateRange.All === DateRange.All)
    }

    @Test
    fun `DateRange All is distinct from Day`() {
        val day = DateRange.Day(Calendar.getInstance().time)
        assertFalse(DateRange.All == day)
    }
}
