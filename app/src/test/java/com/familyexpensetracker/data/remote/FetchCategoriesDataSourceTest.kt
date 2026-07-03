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

class FetchCategoriesDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: FetchCategoriesDataSource

    @Before
    fun setUp() {
        sheetsService = mockk()
        dataSource = FetchCategoriesDataSource(sheetsService, "dummy-range")
    }

    private fun mockSheetValues(rows: List<List<String>>) {
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns ValueRange().setValues(rows)
    }

    @Test
    fun `fetch returns categories map and skips header`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Category", "Subcategory"),
            listOf("Food", "Groceries"),
            listOf("Food", "Dining Out"),
            listOf("Rent", ""),
            listOf("Utilities", "Electricity"),
            listOf("Utilities", "Water"),
        ))

        val result = dataSource.fetch()

        assertEquals(mapOf(
            "Food" to listOf("Groceries", "Dining Out"),
            "Rent" to emptyList<String>(),
            "Utilities" to listOf("Electricity", "Water"),
        ), result)
    }

    @Test
    fun `fetch returns empty map when values are null`() = runBlocking {
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
    fun `fetch includes rows when first row is not a header`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Food", "Groceries"),
            listOf("Food", "Dining Out"),
        ))

        val result = dataSource.fetch()

        assertEquals(mapOf("Food" to listOf("Groceries", "Dining Out")), result)
    }

    @Test
    fun `fetch skips rows with blank category`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Category", "Subcategory"),
            listOf("Food", "Groceries"),
            listOf("", "Orphan"),
        ))

        val result = dataSource.fetch()

        assertEquals(mapOf("Food" to listOf("Groceries")), result)
    }

    @Test
    fun `fetch deduplicates subcategories`() = runBlocking {
        mockSheetValues(listOf(
            listOf("Category", "Subcategory"),
            listOf("Food", "Groceries"),
            listOf("Food", "Groceries"),
            listOf("Food", "Dining Out"),
        ))

        val result = dataSource.fetch()

        assertEquals(mapOf("Food" to listOf("Groceries", "Dining Out")), result)
    }

    @Test
    fun `fetch recognises IncomeCategories header and skips it`() = runBlocking {
        mockSheetValues(listOf(
            listOf("IncomeCategories", "Subcategory"),
            listOf("Salary", "Monthly"),
        ))

        val result = dataSource.fetch()

        assertEquals(mapOf("Salary" to listOf("Monthly")), result)
    }
}
