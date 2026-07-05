package com.familyexpensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class IndianNumberFormatterTest {

    @Test
    fun formatAmount_zero_returnsRupeeZero() {
        assertEquals("₹0.00", IndianNumberFormatter.formatAmount(0.0))
    }

    @Test
    fun formatAmount_smallAmount_noCommas() {
        assertEquals("₹500.00", IndianNumberFormatter.formatAmount(500.0))
    }

    @Test
    fun formatAmount_thousands_westernComma() {
        assertEquals("₹1,234.50", IndianNumberFormatter.formatAmount(1234.50))
    }

    @Test
    fun formatAmount_tenThousands_noExtraComma() {
        assertEquals("₹10,000.00", IndianNumberFormatter.formatAmount(10000.0))
    }

    @Test
    fun formatAmount_lakh_indianGrouping() {
        assertEquals("₹1,00,000.00", IndianNumberFormatter.formatAmount(100000.0))
    }

    @Test
    fun formatAmount_tenLakh_indianGrouping() {
        assertEquals("₹10,00,000.00", IndianNumberFormatter.formatAmount(1000000.0))
    }

    @Test
    fun formatAmount_crore_indianGrouping() {
        assertEquals("₹1,10,00,000.00", IndianNumberFormatter.formatAmount(11000000.0))
    }

    @Test
    fun formatAmount_negativeAmount_preservesSign() {
        assertEquals("−₹500.00", IndianNumberFormatter.formatAmount(-500.0))
    }

    @Test
    fun formatAmount_negativeLakh_preservesSign() {
        assertEquals("−₹1,00,000.00", IndianNumberFormatter.formatAmount(-100000.0))
    }

    @Test
    fun formatAmount_twoDecimalPlaces_alwaysShown() {
        assertEquals("₹1,200.00", IndianNumberFormatter.formatAmount(1200.0))
    }

    @Test
    fun formatAmount_centsNinetyNine_roundsCorrectly() {
        assertEquals("₹123.99", IndianNumberFormatter.formatAmount(123.99))
    }

    @Test
    fun formatAmount_centsOne_showsCorrectly() {
        assertEquals("₹100.01", IndianNumberFormatter.formatAmount(100.01))
    }

    @Test
    fun formatAmount_doesNotContainRawUnformattedNumber() {
        val result = IndianNumberFormatter.formatAmount(100000.0)
        assert(!result.contains("100000")) { "Raw unformatted number must not appear" }
    }

    @Test
    fun formatAmount_negativeAmount_containsUnicodeMinusSign() {
        val result = IndianNumberFormatter.formatAmount(-500.0)
        assert(result.startsWith("−")) { "Negative amounts must start with Unicode minus sign (U+2212)" }
    }
}
