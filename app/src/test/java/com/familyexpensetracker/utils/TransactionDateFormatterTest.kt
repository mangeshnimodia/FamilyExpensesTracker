package com.familyexpensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDateFormatterTest {

    @Test
    fun format_convertsYyyyMmDd_toDdMmYyyy() {
        assertEquals("28/06/2026", TransactionDateFormatter.format("2026/06/28"))
    }

    @Test
    fun format_convertsFirstOfJanuary() {
        assertEquals("01/01/2025", TransactionDateFormatter.format("2025/01/01"))
    }

    @Test
    fun format_convertsLastDayOfYear() {
        assertEquals("31/12/2024", TransactionDateFormatter.format("2024/12/31"))
    }

    @Test
    fun format_returnsInputOnUnparseable() {
        assertEquals("not-a-date", TransactionDateFormatter.format("not-a-date"))
    }

    @Test
    fun format_returnsInputOnEmptyString() {
        assertEquals("", TransactionDateFormatter.format(""))
    }
}
