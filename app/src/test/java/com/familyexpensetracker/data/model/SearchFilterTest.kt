package com.familyexpensetracker.data.model

import org.junit.Assert.*
import org.junit.Test

class SearchFilterTest {

    @Test
    fun `default SearchFilter is empty`() {
        assertTrue(SearchFilter().isEmpty())
    }

    @Test
    fun `SearchFilter with account is not empty`() {
        assertFalse(SearchFilter(account = "Passbook").isEmpty())
    }

    @Test
    fun `SearchFilter with minAmount is not empty`() {
        assertFalse(SearchFilter(minAmount = 100.0).isEmpty())
    }

    @Test
    fun `SearchFilter with maxAmount is not empty`() {
        assertFalse(SearchFilter(maxAmount = 500.0).isEmpty())
    }

    @Test
    fun `SearchFilter with category is not empty`() {
        assertFalse(SearchFilter(category = "Food").isEmpty())
    }

    @Test
    fun `SearchFilter with subcategory is not empty`() {
        assertFalse(SearchFilter(subcategory = "Groceries").isEmpty())
    }

    @Test
    fun `SearchFilter with paymentMethod is not empty`() {
        assertFalse(SearchFilter(paymentMethod = "UPI").isEmpty())
    }

    @Test
    fun `SearchFilter with exactMatch true is not empty`() {
        assertFalse(SearchFilter(exactMatch = true).isEmpty())
    }

    @Test
    fun `blank strings are treated as empty`() {
        assertTrue(SearchFilter(account = "   ", category = "  ", subcategory = " ", paymentMethod = " ").isEmpty())
    }

    @Test
    fun `default values are correct`() {
        val filter = SearchFilter()
        assertEquals("", filter.account)
        assertNull(filter.minAmount)
        assertNull(filter.maxAmount)
        assertEquals("", filter.category)
        assertEquals("", filter.subcategory)
        assertEquals("", filter.paymentMethod)
        assertFalse(filter.exactMatch)
    }

    @Test
    fun `copy with updated field preserves others`() {
        val base = SearchFilter(account = "Savings", category = "Food")
        val updated = base.copy(category = "Transport")
        assertEquals("Savings", updated.account)
        assertEquals("Transport", updated.category)
    }

    @Test
    fun `two SearchFilters with same values are equal`() {
        val a = SearchFilter(account = "Passbook", minAmount = 100.0, exactMatch = true)
        val b = SearchFilter(account = "Passbook", minAmount = 100.0, exactMatch = true)
        assertEquals(a, b)
    }
}
