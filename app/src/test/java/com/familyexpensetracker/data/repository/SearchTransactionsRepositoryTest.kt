package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.SearchTransactionsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTransactionsRepositoryTest {

    private val dataSource = mockk<SearchTransactionsDataSource>()
    private val repository = SearchTransactionsRepository(dataSource)

    @Test
    fun `search delegates to dataSource with default filter`() = runBlocking {
        coEvery { dataSource.search("food", SearchFilter()) } returns emptyList()

        repository.search("food")

        coVerify { dataSource.search("food", SearchFilter()) }
    }

    @Test
    fun `search delegates to dataSource with provided filter`() = runBlocking {
        val filter = SearchFilter(account = "Savings")
        coEvery { dataSource.search("food", filter) } returns emptyList()

        repository.search("food", filter)

        coVerify { dataSource.search("food", filter) }
    }

    @Test
    fun `search returns results from dataSource`() = runBlocking {
        val expected = listOf(
            Transaction(txnId = "1", date = "2026/06/01", amount = -100.0,
                category = "Food", subcategory = "Groceries", description = "food market")
        )
        coEvery { dataSource.search("food", SearchFilter()) } returns expected

        val result = repository.search("food")

        assertEquals(expected, result)
    }

    @Test
    fun `search returns empty list when dataSource returns empty`() = runBlocking {
        coEvery { dataSource.search(any(), any()) } returns emptyList()

        val result = repository.search("xyz")

        assertEquals(emptyList<Transaction>(), result)
    }

    @Test
    fun `search with filter returns filtered results from dataSource`() = runBlocking {
        val filter = SearchFilter(category = "Food")
        val expected = listOf(
            Transaction(txnId = "2", date = "2026/06/01", amount = -200.0,
                category = "Food", subcategory = "Groceries", description = "market")
        )
        coEvery { dataSource.search("", filter) } returns expected

        val result = repository.search("", filter)

        assertEquals(expected, result)
    }
}
