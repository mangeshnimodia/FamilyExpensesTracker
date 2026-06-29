package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddTransactionDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: AddTransactionDataSource

    @Before
    fun setUp() {
        sheetsService = mockk()
        dataSource = AddTransactionDataSource(sheetsService)
    }

    @Test
    fun `add transaction appends values to spreadsheet`() = runBlocking {
        // Arrange
        val transaction = Transaction(
            txnId = "123",
            date = "2026/06/28",
            amount = 50.0,
            category = "Food",
            subcategory = "Lunch",
            paymentMethod = "Cash",
            description = "Tacos",
            account = "Wallet",
            transferId = "trans123"
        )

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val appendRequest = mockk<Sheets.Spreadsheets.Values.Append>()
        val response = AppendValuesResponse()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.append(any(), any(), any()) } returns appendRequest
        every { appendRequest.setValueInputOption(any()) } returns appendRequest
        every { appendRequest.execute() } returns response

        // Act
        val result = dataSource.add(transaction)

        // Assert
        assertEquals(response, result)
        verify {
            values.append(
                any(),
                any(),
                match {
                    val row = it.getValues()[0]
                    row[0] == "123" && 
                    row[2] == "50.0" && 
                    row[3] == "Food" && 
                    row[8] == "trans123"
                }
            )
        }
    }

    @Test
    fun `add multiple transactions appends all rows to spreadsheet`() = runBlocking {
        // Arrange
        val t1 = Transaction(txnId = "1", amount = -100.0, category = "Account Transfer", account = "Bank", transferId = "uuid")
        val t2 = Transaction(txnId = "2", amount = 100.0, category = "Income", subcategory = "Account Transfer", account = "Cash", transferId = "uuid")

        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val appendRequest = mockk<Sheets.Spreadsheets.Values.Append>()
        val response = AppendValuesResponse()

        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.append(any(), any(), any()) } returns appendRequest
        every { appendRequest.setValueInputOption(any()) } returns appendRequest
        every { appendRequest.execute() } returns response

        // Act
        dataSource.add(t1, t2)

        // Assert
        verify {
            values.append(
                any(),
                any(),
                match {
                    val rows = it.getValues()
                    rows.size == 2 &&
                    rows[0][0] == "1" && rows[0][8] == "uuid" &&
                    rows[1][0] == "2" && rows[1][8] == "uuid"
                }
            )
        }
    }
}
