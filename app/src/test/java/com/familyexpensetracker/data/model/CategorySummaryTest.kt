package com.familyexpensetracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategorySummaryTest {

    @Test
    fun `CategorySummary holds expected values`() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -1500.0,
            transactionCount = 5
        )
        assertEquals("Food", summary.category)
        assertEquals(-1500.0, summary.totalAmount, 0.001)
        assertEquals(5, summary.transactionCount)
        assertTrue(summary.subcategories.isEmpty())
    }

    @Test
    fun `CategorySummary with subcategories preserves list`() {
        val sub1 = SubcategoryGroup("Groceries", -800.0, 3)
        val sub2 = SubcategoryGroup("Dining", -700.0, 2)
        val summary = CategorySummary("Food", -1500.0, 5, listOf(sub1, sub2))
        assertEquals(2, summary.subcategories.size)
        assertEquals("Groceries", summary.subcategories[0].subcategory)
    }

    @Test
    fun `CategorySummary equality based on data`() {
        val a = CategorySummary("Food", -1500.0, 5)
        val b = CategorySummary("Food", -1500.0, 5)
        assertEquals(a, b)
    }

    @Test
    fun `CategorySummary copy works correctly`() {
        val original = CategorySummary("Food", -1500.0, 5)
        val copied = original.copy(totalAmount = -2000.0)
        assertEquals(-2000.0, copied.totalAmount, 0.001)
        assertEquals("Food", copied.category)
    }
}
