package com.familyexpensetracker.data.remote

import android.util.Log
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.data.model.TransactionFilter
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class FetchTransactionsDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: FetchTransactionsDataSource

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        sheetsService = mockk()
        dataSource = FetchTransactionsDataSource(sheetsService)
    }

    @Test
    fun `fetch returns list of transactions when successful`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Food", "Groceries", "UPI", "Bought milk", "Savings", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        // Act
        val result = dataSource.fetch()

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].txnId)
        assertEquals(100.5, result[0].amount, 0.0)
        assertEquals("Food", result[0].category)
    }

    @Test
    fun `fetch filters by date range when provided`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Food", "Groceries", "UPI", "Bought milk", "Savings", ""),
            listOf("2", "2026/07/01", "200.0", "Rent", "", "Bank", "Monthly rent", "Savings", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val dateRange = DateRange.Month(2026, Calendar.JUNE)

        // Act
        val result = dataSource.fetch(dateRange)

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].txnId)
    }

    @Test
    fun `fetch filters by accounts when provided`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Food", "Groceries", "UPI", "Bought milk", "Savings", ""),
            listOf("2", "2026/06/28", "200.0", "Rent", "", "Bank", "Monthly rent", "Wallet", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val filter = TransactionFilter(selectedAccounts = listOf("Wallet"))

        // Act
        val result = dataSource.fetch(filter = filter)

        // Assert
        assertEquals(1, result.size)
        assertEquals("2", result[0].txnId)
        assertEquals("Wallet", result[0].account)
    }

    @Test
    fun `fetch filters by transaction type income`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Food", "Groceries", "UPI", "Income", "Savings", ""),
            listOf("2", "2026/06/28", "-50.0", "Rent", "", "Bank", "Expense", "Wallet", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val filter = TransactionFilter(type = FilterType.INCOME)

        // Act
        val result = dataSource.fetch(filter = filter)

        // Assert
        assertEquals(1, result.size)
        assertEquals("1", result[0].txnId)
        assertTrue(result[0].amount > 0)
    }

    @Test
    fun `fetch filters by transaction type expense`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Food", "Groceries", "UPI", "Income", "Savings", ""),
            listOf("2", "2026/06/28", "-50.0", "Rent", "", "Bank", "Expense", "Wallet", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val filter = TransactionFilter(type = FilterType.EXPENSE)

        // Act
        val result = dataSource.fetch(filter = filter)

        // Assert
        assertEquals(1, result.size)
        assertEquals("2", result[0].txnId)
        assertTrue(result[0].amount < 0)
    }

    @Test
    fun `fetch excludes account transfer from all types`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            listOf("1", "2026/06/28", "100.5", "Income", "Account Transfer", "UPI", "Transfer", "Savings", ""),
            listOf("2", "2026/06/28", "-100.5", "Account Transfer", "", "UPI", "Transfer", "Savings", ""),
            listOf("3", "2026/06/28", "200.0", "Salary", "", "Bank", "Real Income", "Savings", ""),
            listOf("4", "2026/06/28", "-200.0", "Food", "", "Bank", "Real Expense", "Savings", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        // Act & Assert for BALANCE
        val resultBalance = dataSource.fetch(filter = TransactionFilter(type = FilterType.BALANCE))
        assertEquals(2, resultBalance.size)
        assertTrue(resultBalance.none { it.txnId == "1" || it.txnId == "2" })

        // Act & Assert for INCOME
        val resultIncome = dataSource.fetch(filter = TransactionFilter(type = FilterType.INCOME))
        assertEquals(1, resultIncome.size)
        assertEquals("3", resultIncome[0].txnId)

        // Act & Assert for EXPENSE
        val resultExpense = dataSource.fetch(filter = TransactionFilter(type = FilterType.EXPENSE))
        assertEquals(1, resultExpense.size)
        assertEquals("4", resultExpense[0].txnId)
    }

    @Test
    fun `fetch returns empty list when no values in sheet`() = runBlocking {
        // Arrange
        val valueRange = ValueRange().setValues(null)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        // Act
        val result = dataSource.fetch()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetch applies all four filters in sequence`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("txnId", "date", "amount", "category", "subcategory", "paymentMethod", "description", "account", "transferId"),
            // 1. Matches all: June 2026, Wallet, Income (positive amount), Not a transfer
            listOf("txn1", "2026/06/15", "100.0", "Food", "Groceries", "UPI", "Valid", "Wallet", ""),
            // 2. Fails Date Range (July instead of June)
            listOf("txn2", "2026/07/15", "100.0", "Food", "Groceries", "UPI", "Wrong Date", "Wallet", ""),
            // 3. Fails Account Filter (Savings instead of Wallet)
            listOf("txn3", "2026/06/15", "100.0", "Food", "Groceries", "UPI", "Wrong Account", "Savings", ""),
            // 4. Fails Type Filter (Negative amount for INCOME filter)
            listOf("txn4", "2026/06/15", "-50.0", "Food", "Groceries", "UPI", "Wrong Type", "Wallet", ""),
            // 5. Fails Exclusion (Account Transfer category)
            listOf("txn5", "2026/06/15", "200.0", "Account Transfer", "", "UPI", "Transfer Category", "Wallet", ""),
            // 6. Fails Exclusion (Income/Account Transfer subcategory)
            listOf("txn6", "2026/06/15", "200.0", "Income", "Account Transfer", "UPI", "Transfer Subcat", "Wallet", "")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val dateRange = DateRange.Month(2026, Calendar.JUNE)
        val filter = TransactionFilter(
            selectedAccounts = listOf("Wallet"),
            type = FilterType.INCOME
        )

        // Act
        val result = dataSource.fetch(dateRange = dateRange, filter = filter)

        // Assert
        assertEquals(1, result.size)
        assertEquals("txn1", result[0].txnId)
        assertEquals("Wallet", result[0].account)
        assertTrue(result[0].amount > 0)
        assertEquals("Food", result[0].category)
    }
}
