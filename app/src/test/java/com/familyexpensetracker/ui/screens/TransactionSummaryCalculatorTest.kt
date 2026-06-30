package com.familyexpensetracker.ui.screens

import com.familyexpensetracker.data.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionSummaryCalculatorTest {

    private lateinit var calculator: TransactionSummaryCalculator

    private val food1 = Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
    private val food2 = Transaction(txnId = "2", date = "2026/06/16", amount = -300.0, category = "Food", subcategory = "Dining")
    private val food3 = Transaction(txnId = "3", date = "2026/06/17", amount = -200.0, category = "Food", subcategory = "Groceries")
    private val transport = Transaction(txnId = "4", date = "2026/06/15", amount = -150.0, category = "Transport", subcategory = "Fuel")
    private val income = Transaction(txnId = "5", date = "2026/06/01", amount = 50000.0, category = "Income", subcategory = "Salary")

    @Before
    fun setUp() {
        calculator = TransactionSummaryCalculator()
    }

    // --- calculateCategorySummaries ---

    @Test
    fun `calculateCategorySummaries groups by category`() {
        val result = calculator.calculateCategorySummaries(listOf(food1, food2, transport))
        assertEquals(2, result.size)
        val foodSummary = result.find { it.category == "Food" }!!
        assertEquals(-800.0, foodSummary.totalAmount, 0.001)
        assertEquals(2, foodSummary.transactionCount)
    }

    @Test
    fun `calculateCategorySummaries sorts by absolute amount descending`() {
        val result = calculator.calculateCategorySummaries(listOf(food1, food2, food3, transport, income))
        assertEquals("Income", result[0].category)
        assertEquals("Food", result[1].category)
        assertEquals("Transport", result[2].category)
    }

    @Test
    fun `calculateCategorySummaries includes subcategories`() {
        val result = calculator.calculateCategorySummaries(listOf(food1, food2, food3))
        val foodSummary = result.find { it.category == "Food" }!!
        assertEquals(2, foodSummary.subcategories.size)
        val groceries = foodSummary.subcategories.find { it.subcategory == "Groceries" }!!
        assertEquals(-700.0, groceries.totalAmount, 0.001)
        assertEquals(2, groceries.transactionCount)
    }

    @Test
    fun `calculateCategorySummaries with empty list returns empty`() {
        assertTrue(calculator.calculateCategorySummaries(emptyList()).isEmpty())
    }

    @Test
    fun `calculateCategorySummaries single transaction`() {
        val result = calculator.calculateCategorySummaries(listOf(food1))
        assertEquals(1, result.size)
        assertEquals(-500.0, result[0].totalAmount, 0.001)
        assertEquals(1, result[0].transactionCount)
    }

    // --- calculateSubcategoryGroups ---

    @Test
    fun `calculateSubcategoryGroups groups by subcategory`() {
        val result = calculator.calculateSubcategoryGroups(listOf(food1, food2, food3))
        assertEquals(2, result.size)
        val groceries = result.find { it.subcategory == "Groceries" }!!
        assertEquals(-700.0, groceries.totalAmount, 0.001)
        assertEquals(2, groceries.transactionCount)
    }

    @Test
    fun `calculateSubcategoryGroups sorts by absolute amount descending`() {
        val result = calculator.calculateSubcategoryGroups(listOf(food1, food2, food3))
        assertEquals("Groceries", result[0].subcategory)
        assertEquals("Dining", result[1].subcategory)
    }

    @Test
    fun `calculateSubcategoryGroups includes transactions`() {
        val result = calculator.calculateSubcategoryGroups(listOf(food1, food3))
        val groceries = result.find { it.subcategory == "Groceries" }!!
        assertEquals(2, groceries.transactions.size)
    }

    @Test
    fun `calculateSubcategoryGroups with empty list returns empty`() {
        assertTrue(calculator.calculateSubcategoryGroups(emptyList()).isEmpty())
    }

    // --- calculateMonthSummaries ---

    @Test
    fun `calculateMonthSummaries groups by year and month`() {
        val mayTxn = Transaction(txnId = "6", date = "2026/05/15", amount = -400.0, category = "Food", subcategory = "")
        val result = calculator.calculateMonthSummaries(listOf(food1, food2, transport, mayTxn))
        assertEquals(2, result.size)
        val june = result.find { it.month == 5 }!! // Calendar.JUNE = 5
        assertEquals(3, june.transactionCount)
        assertEquals(-950.0, june.totalAmount, 0.001)
    }

    @Test
    fun `calculateMonthSummaries sorts most recent first`() {
        val may = Transaction(txnId = "6", date = "2026/05/15", amount = -400.0, category = "Food", subcategory = "")
        val result = calculator.calculateMonthSummaries(listOf(may, food1))
        assertEquals(5, result[0].month) // June first
        assertEquals(4, result[1].month) // May second
    }

    @Test
    fun `calculateMonthSummaries sorts across years`() {
        val lastYear = Transaction(txnId = "6", date = "2025/06/15", amount = -200.0, category = "Food", subcategory = "")
        val result = calculator.calculateMonthSummaries(listOf(food1, lastYear))
        assertEquals(2026, result[0].year)
        assertEquals(2025, result[1].year)
    }

    @Test
    fun `calculateMonthSummaries skips unparseable dates`() {
        val bad = Transaction(txnId = "6", date = "invalid", amount = -100.0, category = "Food", subcategory = "")
        val result = calculator.calculateMonthSummaries(listOf(food1, bad))
        assertEquals(1, result.size)
    }

    @Test
    fun `calculateMonthSummaries with empty list returns empty`() {
        assertTrue(calculator.calculateMonthSummaries(emptyList()).isEmpty())
    }

    // --- calculateFYMonthSummaries ---

    @Test
    fun `calculateFYMonthSummaries returns exactly 12 entries`() {
        val result = calculator.calculateFYMonthSummaries(emptyList(), fyStartYear = 2026)
        assertEquals(12, result.size)
    }

    @Test
    fun `calculateFYMonthSummaries first entry is April of start year`() {
        val result = calculator.calculateFYMonthSummaries(emptyList(), fyStartYear = 2026)
        assertEquals(2026, result[0].year)
        assertEquals(3, result[0].month) // Calendar.APRIL = 3
    }

    @Test
    fun `calculateFYMonthSummaries last entry is March of following year`() {
        val result = calculator.calculateFYMonthSummaries(emptyList(), fyStartYear = 2026)
        assertEquals(2027, result[11].year)
        assertEquals(2, result[11].month) // Calendar.MARCH = 2
    }

    @Test
    fun `calculateFYMonthSummaries fills zero for months with no transactions`() {
        val result = calculator.calculateFYMonthSummaries(emptyList(), fyStartYear = 2026)
        assertTrue(result.all { it.totalAmount == 0.0 })
        assertTrue(result.all { it.transactionCount == 0 })
    }

    @Test
    fun `calculateFYMonthSummaries sums transactions into correct month`() {
        val result = calculator.calculateFYMonthSummaries(listOf(food1, food2, transport), fyStartYear = 2026)
        // food1 + food2 + transport are all in June 2026 (month=5)
        val june = result.find { it.year == 2026 && it.month == 5 }!!
        assertEquals(-950.0, june.totalAmount, 0.001)
        assertEquals(3, june.transactionCount)
        // May should be zero
        val may = result.find { it.year == 2026 && it.month == 4 }!!
        assertEquals(0.0, may.totalAmount, 0.001)
    }

    @Test
    fun `calculateFYMonthSummaries handles Jan-Mar in following year`() {
        val janTxn = Transaction(txnId = "9", date = "2027/01/10", amount = -200.0, category = "Food", subcategory = "")
        val result = calculator.calculateFYMonthSummaries(listOf(janTxn), fyStartYear = 2026)
        val jan = result.find { it.year == 2027 && it.month == 0 }!! // Calendar.JANUARY = 0
        assertEquals(-200.0, jan.totalAmount, 0.001)
    }
}
