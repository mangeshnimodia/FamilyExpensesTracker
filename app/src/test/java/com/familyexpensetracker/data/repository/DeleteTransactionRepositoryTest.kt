package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.remote.DeleteTransactionDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DeleteTransactionRepositoryTest {

    private val dataSource = mockk<DeleteTransactionDataSource>()
    private val repository = DeleteTransactionRepository(dataSource)

    @Test
    fun `delete delegates to dataSource`() = runBlocking {
        val txnId = "123"
        coEvery { dataSource.delete(txnId) } returns Unit

        repository.delete(txnId)

        coVerify { dataSource.delete(txnId) }
    }
}
