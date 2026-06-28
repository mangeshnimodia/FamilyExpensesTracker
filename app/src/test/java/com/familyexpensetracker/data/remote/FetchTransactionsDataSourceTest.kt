package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.DateRange
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.mockk.every
import io.mockk.mockk
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
}
