package com.familyexpensetracker.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

    @Test
    fun `fetch returns unique accounts and skips header`() = runBlocking {
        // Arrange
        val mockValues = listOf(
            listOf("Account"),
            listOf("Savings"),
            listOf("Cash"),
            listOf("Savings"),
            listOf("Wallet")
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
        assertEquals(listOf("Savings", "Cash", "Wallet"), result)
    }
}
