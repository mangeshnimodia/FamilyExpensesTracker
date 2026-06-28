package com.familyexpensetracker.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchCategoriesDataSourceTest {

    private lateinit var sheetsService: Sheets

    @Before
    fun setUp() {
        sheetsService = mockk()
    }

    @Test
    fun `fetch returns categories map and skips header`() = runBlocking {
        // Arrange
        val dataSource = FetchCategoriesDataSource(sheetsService, "dummy-range")
        val mockValues = listOf(
            listOf("Category", "Subcategory"),
            listOf("Food", "Groceries"),
            listOf("Food", "Dining Out"),
            listOf("Rent", ""),
            listOf("Utilities", "Electricity"),
            listOf("Utilities", "Water")
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
        val expected = mapOf(
            "Food" to listOf("Groceries", "Dining Out"),
            "Rent" to emptyList(),
            "Utilities" to listOf("Electricity", "Water")
        )
        assertEquals(expected, result)
    }
}
