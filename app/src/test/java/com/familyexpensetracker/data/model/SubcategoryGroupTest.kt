package com.familyexpensetracker.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubcategoryGroupTest {

    @Test
    fun `SubcategoryGroup holds expected values`() {
        val group = SubcategoryGroup(
            subcategory = "Groceries",
            totalAmount = -800.0,
            transactionCount = 3
        )
        assertEquals("Groceries", group.subcategory)
        assertEquals(-800.0, group.totalAmount, 0.001)
        assertEquals(3, group.transactionCount)
        assertTrue(group.transactions.isEmpty())
    }

    @Test
    fun `SubcategoryGroup with transactions preserves list`() {
        val txn = Transaction(txnId = "1", amount = -200.0, category = "Food", subcategory = "Groceries")
        val group = SubcategoryGroup("Groceries", -200.0, 1, listOf(txn))
        assertEquals(1, group.transactions.size)
        assertEquals("1", group.transactions[0].txnId)
    }

    @Test
    fun `SubcategoryGroup equality based on data`() {
        val a = SubcategoryGroup("Groceries", -800.0, 3)
        val b = SubcategoryGroup("Groceries", -800.0, 3)
        assertEquals(a, b)
    }

    @Test
    fun `SubcategoryGroup copy works correctly`() {
        val original = SubcategoryGroup("Groceries", -800.0, 3)
        val copied = original.copy(transactionCount = 10)
        assertEquals(10, copied.transactionCount)
        assertEquals("Groceries", copied.subcategory)
    }
}
