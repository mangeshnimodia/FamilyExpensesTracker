package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.AddTransactionDataSource
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AddTransactionRepositoryTest {

    private val dataSource = mockk<AddTransactionDataSource>()
    private val repository = AddTransactionRepository(dataSource)

    @Test
    fun `add delegates to dataSource`() = runBlocking {
        val transaction = mockk<Transaction>()
        val response = mockk<AppendValuesResponse>()
        coEvery { dataSource.add(any()) } returns response

        val result = repository.add(transaction)

        assertEquals(response, result)
        coVerify { dataSource.add(transaction) }
    }

    @Test
    fun `add multiple transactions delegates to dataSource`() = runBlocking {
        val t1 = mockk<Transaction>()
        val t2 = mockk<Transaction>()
        val response = mockk<AppendValuesResponse>()
        coEvery { dataSource.add(any(), any()) } returns response

        val result = repository.add(t1, t2)

        assertEquals(response, result)
        coVerify { dataSource.add(t1, t2) }
    }
}
