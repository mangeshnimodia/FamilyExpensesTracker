package com.familyexpensetracker.ui.screens

import com.familyexpensetracker.data.model.DateRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class PeriodNavigatorTest {

    private lateinit var navigator: PeriodNavigator

    @Before
    fun setUp() {
        navigator = PeriodNavigator()
    }

    // --- Day navigation ---

    @Test
    fun `previous Day returns previous day`() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }
        val current = DateRange.Day(cal.time)
        val result = navigator.previous(current) as DateRange.Day
        val resultCal = Calendar.getInstance().apply { time = result.date }
        assertEquals(14, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(2026, resultCal.get(Calendar.YEAR))
    }

    @Test
    fun `next Day returns next day`() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }
        val current = DateRange.Day(cal.time)
        val result = navigator.next(current) as DateRange.Day
        val resultCal = Calendar.getInstance().apply { time = result.date }
        assertEquals(16, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH))
        assertEquals(2026, resultCal.get(Calendar.YEAR))
    }

    @Test
    fun `previous Day crosses month boundary`() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 1) }
        val result = navigator.previous(DateRange.Day(cal.time)) as DateRange.Day
        val resultCal = Calendar.getInstance().apply { time = result.date }
        assertEquals(31, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MAY, resultCal.get(Calendar.MONTH))
    }

    @Test
    fun `next Day crosses month boundary`() {
        val cal = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 30) }
        val result = navigator.next(DateRange.Day(cal.time)) as DateRange.Day
        val resultCal = Calendar.getInstance().apply { time = result.date }
        assertEquals(1, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JULY, resultCal.get(Calendar.MONTH))
    }

    // --- Month navigation ---

    @Test
    fun `previous Month returns previous month`() {
        val current = DateRange.Month(2026, Calendar.JUNE)
        val result = navigator.previous(current) as DateRange.Month
        assertEquals(2026, result.year)
        assertEquals(Calendar.MAY, result.month)
    }

    @Test
    fun `next Month returns next month`() {
        val current = DateRange.Month(2026, Calendar.JUNE)
        val result = navigator.next(current) as DateRange.Month
        assertEquals(2026, result.year)
        assertEquals(Calendar.JULY, result.month)
    }

    @Test
    fun `previous Month from January wraps to December of prior year`() {
        val current = DateRange.Month(2026, Calendar.JANUARY)
        val result = navigator.previous(current) as DateRange.Month
        assertEquals(2025, result.year)
        assertEquals(Calendar.DECEMBER, result.month)
    }

    @Test
    fun `next Month from December wraps to January of next year`() {
        val current = DateRange.Month(2026, Calendar.DECEMBER)
        val result = navigator.next(current) as DateRange.Month
        assertEquals(2027, result.year)
        assertEquals(Calendar.JANUARY, result.month)
    }

    // --- FinancialYear navigation ---

    @Test
    fun `previous FinancialYear decrements startYear`() {
        val current = DateRange.FinancialYear(2025)
        val result = navigator.previous(current) as DateRange.FinancialYear
        assertEquals(2024, result.startYear)
    }

    @Test
    fun `next FinancialYear increments startYear`() {
        val current = DateRange.FinancialYear(2025)
        val result = navigator.next(current) as DateRange.FinancialYear
        assertEquals(2026, result.startYear)
    }

    // --- All / Custom: no navigation ---

    @Test
    fun `previous All returns All unchanged`() {
        assertEquals(DateRange.All, navigator.previous(DateRange.All))
    }

    @Test
    fun `next All returns All unchanged`() {
        assertEquals(DateRange.All, navigator.next(DateRange.All))
    }

    @Test
    fun `previous Custom returns same Custom unchanged`() {
        val start = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }.time
        val end = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 30) }.time
        val custom = DateRange.Custom(start, end)
        assertEquals(custom, navigator.previous(custom))
    }

    @Test
    fun `next Custom returns same Custom unchanged`() {
        val start = Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }.time
        val end = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 30) }.time
        val custom = DateRange.Custom(start, end)
        assertEquals(custom, navigator.next(custom))
    }

    // --- isNavigable ---

    @Test
    fun `isNavigable returns true for Day`() {
        val cal = Calendar.getInstance()
        assertTrue(navigator.isNavigable(DateRange.Day(cal.time)))
    }

    @Test
    fun `isNavigable returns true for Month`() {
        assertTrue(navigator.isNavigable(DateRange.Month(2026, Calendar.JUNE)))
    }

    @Test
    fun `isNavigable returns true for FinancialYear`() {
        assertTrue(navigator.isNavigable(DateRange.FinancialYear(2025)))
    }

    @Test
    fun `isNavigable returns false for All`() {
        assertFalse(navigator.isNavigable(DateRange.All))
    }

    @Test
    fun `isNavigable returns false for Custom`() {
        val start = Calendar.getInstance().time
        val end = Calendar.getInstance().time
        assertFalse(navigator.isNavigable(DateRange.Custom(start, end)))
    }
}
