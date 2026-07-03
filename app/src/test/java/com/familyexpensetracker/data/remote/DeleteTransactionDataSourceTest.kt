package com.familyexpensetracker.data.remote

import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTransactionDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: DeleteTransactionDataSource

    @Before
    fun setUp() {
        sheetsService = mockk()
        dataSource = DeleteTransactionDataSource(sheetsService)
    }

    @Test
    fun `delete removes correct row when txnId found`() = runBlocking {
        // Arrange
        val txnId = "target-txn"
        val mockValues = listOf(
            listOf("txnId"),
            listOf("txn-1"),
            listOf("target-txn"),
            listOf("txn-2")
        )
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        // Mock getting sheetId
        val spreadsheet = mockk<Spreadsheet>()
        val sheet = mockk<Sheet>()
        val sheetProperties = mockk<SheetProperties>()
        
        every { spreadsheets.get(any()) } returns mockk<Sheets.Spreadsheets.Get>().also {
            every { it.execute() } returns spreadsheet
        }
        every { spreadsheet.sheets } returns listOf(sheet)
        every { sheet.properties } returns sheetProperties
        every { sheetProperties.title } returns AppConstants.SHEET_NAME_TRANSACTIONS
        every { sheetProperties.sheetId } returns 123

        // Mock batchUpdate
        val batchUpdateRequest = mockk<Sheets.Spreadsheets.BatchUpdate>()
        every { spreadsheets.batchUpdate(any(), any()) } returns batchUpdateRequest
        every { batchUpdateRequest.execute() } returns BatchUpdateSpreadsheetResponse()

        // Act
        dataSource.delete(txnId)

        // Assert
        verify {
            spreadsheets.batchUpdate(any(), match {
                val deleteReq = it.requests[0].deleteDimension
                deleteReq.range.startIndex == 2 && deleteReq.range.endIndex == 3
            })
        }
    }

    @Test
    fun `delete does nothing when values are null`() = runBlocking {
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns ValueRange().setValues(null)

        dataSource.delete("any-id")

        verify(exactly = 0) { spreadsheets.batchUpdate(any(), any()) }
    }

    @Test
    fun `delete throws when Transactions sheet not found`() = runBlocking {
        val mockValues = listOf(listOf("txnId"), listOf("target-txn"))
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns ValueRange().setValues(mockValues)

        val spreadsheet = mockk<Spreadsheet>()
        every { spreadsheets.get(any()) } returns mockk<Sheets.Spreadsheets.Get>().also {
            every { it.execute() } returns spreadsheet
        }
        every { spreadsheet.sheets } returns emptyList()

        var threw = false
        try {
            dataSource.delete("target-txn")
        } catch (e: Exception) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun `delete does nothing when txnId not found`() = runBlocking {
        // Arrange
        val txnId = "not-found"
        val mockValues = listOf(listOf("txnId"), listOf("txn-1"))
        val valueRange = ValueRange().setValues(mockValues)

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns valueRange

        // Act
        dataSource.delete(txnId)

        // Assert
        verify(exactly = 0) {
            spreadsheets.batchUpdate(any(), any())
        }
    }
}
