package com.familyexpensetracker.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FetchAccountsDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: FetchAccountsDataSource

    @Before
    fun setUp() {
        sheetsService = mockk()
        dataSource = FetchAccountsDataSource(sheetsService)
    }

    private fun mockSheetValues(rows: List<List<String>>) {
        val valueRange = ValueRange().setValues(rows)
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange
    }

    @Test
    fun `fetch returns unique accounts and skips header`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Account"),
            listOf("Savings"),
            listOf("Cash"),
            listOf("Savings"),
            listOf("Wallet"),
        ))

        val result = dataSource.fetch()

        assertEquals(listOf("Savings", "Cash", "Wallet"), result)
    }

    @Test
    fun `fetch returns empty list when values are null`() = runBlocking {
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns ValueRange().setValues(null)

        val result = dataSource.fetch()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `fetch includes all rows when first row is not a header`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Savings"),
            listOf("Cash"),
        ))

        val result = dataSource.fetch()

        assertEquals(listOf("Savings", "Cash"), result)
    }

    @Test
    fun `fetch filters out blank account names`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Account"),
            listOf("Savings"),
            listOf(""),
            listOf("Cash"),
        ))

        val result = dataSource.fetch()

        assertEquals(listOf("Savings", "Cash"), result)
    }
}
