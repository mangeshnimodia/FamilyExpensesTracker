package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateTransactionDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: UpdateTransactionDataSource

    @Before
    fun setUp() {
        sheetsService = mockk()
        dataSource = UpdateTransactionDataSource(sheetsService)
    }

    @Test
    fun `update modifies correct row when txnId found`() = runBlocking {
        // Arrange
        val transaction = Transaction(
            txnId = "target-txn",
            date = "2023/10/27",
            amount = -100.0,
            category = "Food",
            subcategory = "Dinner",
            paymentMethod = "Card",
            description = "Pizza",
            account = "Bank",
            transferId = null
        )
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
        every { values.get(any(), AppConstants.RANGE_TXN_IDS) } returns getRequest
        every { getRequest.execute() } returns valueRange

        val updateRequest = mockk<Sheets.Spreadsheets.Values.Update>()
        every { values.update(any(), "Transactions!A3:I3", any()) } returns updateRequest
        every { updateRequest.setValueInputOption("USER_ENTERED") } returns updateRequest
        every { updateRequest.execute() } returns UpdateValuesResponse()

        // Act
        dataSource.update(transaction)

        // Assert
        verify {
            values.update(any(), "Transactions!A3:I3", match {
                it.getValues()[0][0] == "target-txn" &&
                it.getValues()[0][1] == "2023/10/27" &&
                it.getValues()[0][2] == "-100.0"
            })
        }
    }

    @Test
    fun `update does nothing when values are null`() = runBlocking {
        val transaction = Transaction(
            txnId = "any", date = "2023/10/27", amount = -100.0,
            category = "Food", subcategory = "", paymentMethod = "Card",
            description = "Pizza", account = "Bank", transferId = null
        )
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values.get(any(), any()) } returns getRequest
        every { getRequest.execute() } returns ValueRange().setValues(null)

        dataSource.update(transaction)

        verify(exactly = 0) { values.update(any(), any(), any()) }
    }

    @Test
    fun `update does nothing when txnId not found`() = runBlocking {
        // Arrange
        val transaction = Transaction(
            txnId = "not-found",
            date = "2023/10/27",
            amount = -100.0,
            category = "Food",
            subcategory = "Dinner",
            paymentMethod = "Card",
            description = "Pizza",
            account = "Bank",
            transferId = null
        )
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
        dataSource.update(transaction)

        // Assert
        verify(exactly = 0) {
            values.update(any(), any(), any())
        }
    }
}
