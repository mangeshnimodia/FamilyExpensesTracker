package com.familyexpensetracker.data.repository

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
    fun `search delegates to dataSource`() = runBlocking {
        coEvery { dataSource.search("food") } returns emptyList()

        repository.search("food")

        coVerify { dataSource.search("food") }
    }

    @Test
    fun `search returns results from dataSource`() = runBlocking {
        val expected = listOf(
            Transaction(txnId = "1", date = "2026/06/01", amount = -100.0,
                category = "Food", subcategory = "Groceries", description = "food market")
        )
        coEvery { dataSource.search("food") } returns expected

        val result = repository.search("food")

        assertEquals(expected, result)
    }

    @Test
    fun `search returns empty list when dataSource returns empty`() = runBlocking {
        coEvery { dataSource.search(any()) } returns emptyList()

        val result = repository.search("xyz")

        assertEquals(emptyList<Transaction>(), result)
    }
}
