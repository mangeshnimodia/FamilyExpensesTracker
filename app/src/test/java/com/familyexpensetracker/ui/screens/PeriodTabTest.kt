package com.familyexpensetracker.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class PeriodTabTest {

    @Test
    fun `PeriodTab has exactly four values`() {
        assertEquals(4, PeriodTab.entries.size)
    }

    @Test
    fun `PeriodTab values are in expected order`() {
        val expected = listOf(PeriodTab.Daily, PeriodTab.Monthly, PeriodTab.Yearly, PeriodTab.All)
        assertEquals(expected, PeriodTab.entries.toList())
    }

    @Test
    fun `PeriodTab valueOf returns correct entry`() {
        assertEquals(PeriodTab.Daily, PeriodTab.valueOf("Daily"))
        assertEquals(PeriodTab.Monthly, PeriodTab.valueOf("Monthly"))
        assertEquals(PeriodTab.Yearly, PeriodTab.valueOf("Yearly"))
        assertEquals(PeriodTab.All, PeriodTab.valueOf("All"))
    }
}
