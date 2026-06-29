package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FetchTransactionsRepositoryTest {

    private val dataSource = mockk<FetchTransactionsDataSource>()
    private val repository = FetchTransactionsRepository(dataSource)

    @Test
    fun `fetch delegates to dataSource`() = runBlocking {
        val transactions = listOf(mockk<Transaction>())
        val filter = TransactionFilter()
        coEvery { dataSource.fetch(any(), any()) } returns transactions

        val result = repository.fetch(null, filter)

        assertEquals(transactions, result)
        coVerify { dataSource.fetch(null, filter) }
    }
}
