package com.familyexpensetracker.data.remote

import android.util.Log
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

class SearchTransactionsDataSourceTest {

    private lateinit var sheetsService: Sheets
    private lateinit var dataSource: SearchTransactionsDataSource

    private val header = listOf(
        "txnId", "date", "amount", "category", "subcategory",
        "paymentMethod", "description", "account", "transferId"
    )

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        sheetsService = mockk()
        dataSource = SearchTransactionsDataSource(sheetsService)
    }

    private fun mockSheet(rows: List<List<Any>>) {
        val valueRange = ValueRange().setValues(listOf(header) + rows)
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values[any(), any()] } returns getRequest
        every { getRequest.execute() } returns valueRange
    }

    @Test
    fun `search returns empty list for blank query`() = runBlocking {
        val result = dataSource.search("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list for empty string query`() = runBlocking {
        val result = dataSource.search("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search matches description exactly`() = runBlocking {
        mockSheet(listOf(
            listOf("1", "2026/06/01", "100.0", "Food", "Groceries", "UPI", "milk purchase", "Savings", ""),
            listOf("2", "2026/06/02", "50.0",  "Food", "Snacks",    "UPI", "coffee",        "Savings", ""),
        ))

        val result = dataSource.search("milk")

        assertEquals(1, result.size)
        assertEquals("1", result[0].txnId)
        assertEquals("milk purchase", result[0].description)
    }

    @Test
    fun `search is case insensitive`() = runBlocking {
        mockSheet(listOf(
            listOf("1", "2026/06/01", "200.0", "Food", "Groceries", "UPI", "MILK Purchase", "Savings", ""),
        ))

        val result = dataSource.search("milk")

        assertEquals(1, result.size)
        assertEquals("1", result[0].txnId)
    }

    @Test
    fun `search returns no matches when query not in any description`() = runBlocking {
        mockSheet(listOf(
            listOf("1", "2026/06/01", "100.0", "Food", "Groceries", "UPI", "bread", "Savings", ""),
        ))

        val result = dataSource.search("xyz_no_match")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search maps all 9 fields correctly`() = runBlocking {
        mockSheet(listOf(
            listOf("txn-42", "2026/06/15", "350.5", "Transport", "Fuel", "Cash", "petrol fill", "Wallet", "TRF-1"),
        ))

        val result = dataSource.search("petrol")

        assertEquals(1, result.size)
        val t = result[0]
        assertEquals("txn-42",      t.txnId)
        assertEquals("2026/06/15",  t.date)
        assertEquals(350.5,         t.amount, 0.0)
        assertEquals("Transport",   t.category)
        assertEquals("Fuel",        t.subcategory)
        assertEquals("Cash",        t.paymentMethod)
        assertEquals("petrol fill", t.description)
        assertEquals("Wallet",      t.account)
        assertEquals("TRF-1",       t.transferId)
    }

    @Test
    fun `search defaults missing columns to empty string`() = runBlocking {
        val valueRange = ValueRange().setValues(listOf(
            header,
            listOf("short-row"), // only txnId, all others missing
        ))
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values[any(), any()] } returns getRequest
        every { getRequest.execute() } returns valueRange

        // description col is missing -> empty string -> matches blank-ish queries won't match "anything"
        val result = dataSource.search("something")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list when sheet has no data rows`() = runBlocking {
        val valueRange = ValueRange().setValues(listOf(header))
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values[any(), any()] } returns getRequest
        every { getRequest.execute() } returns valueRange

        val result = dataSource.search("food")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list when sheet values are null`() = runBlocking {
        val valueRange = ValueRange().setValues(null)
        val spreadsheets = mockk<Sheets.Spreadsheets>()
        val values = mockk<Sheets.Spreadsheets.Values>()
        val getRequest = mockk<Sheets.Spreadsheets.Values.Get>()
        every { sheetsService.spreadsheets() } returns spreadsheets
        every { spreadsheets.values() } returns values
        every { values[any(), any()] } returns getRequest
        every { getRequest.execute() } returns valueRange

        val result = dataSource.search("food")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search amount defaults to 0 when not parseable`() = runBlocking {
        mockSheet(listOf(
            listOf("1", "2026/06/01", "not-a-number", "Food", "Groceries", "UPI", "bad amount row", "Savings", ""),
        ))

        val result = dataSource.search("bad amount")

        assertEquals(1, result.size)
        assertEquals(0.0, result[0].amount, 0.0)
    }

    @Test
    fun `search returns multiple matches`() = runBlocking {
        mockSheet(listOf(
            listOf("1", "2026/06/01", "100.0", "Food",      "Groceries", "UPI", "market visit",  "Savings", ""),
            listOf("2", "2026/06/02", "200.0", "Transport", "Fuel",      "UPI", "market parking", "Savings", ""),
            listOf("3", "2026/06/03", "50.0",  "Food",      "Snacks",    "UPI", "coffee",         "Savings", ""),
        ))

        val result = dataSource.search("market")

        assertEquals(2, result.size)
        assertTrue(result.any { it.txnId == "1" })
        assertTrue(result.any { it.txnId == "2" })
    }
}
