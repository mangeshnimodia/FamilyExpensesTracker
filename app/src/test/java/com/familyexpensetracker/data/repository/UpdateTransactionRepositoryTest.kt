package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.UpdateTransactionDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UpdateTransactionRepositoryTest {

    private val dataSource = mockk<UpdateTransactionDataSource>()
    private val repository = UpdateTransactionRepository(dataSource)

    @Test
    fun `update calls data source`() = runBlocking {
        // Arrange
        val transaction = Transaction(
            txnId = "txn-1",
            date = "2023/10/27",
            amount = -100.0,
            category = "Food",
            subcategory = "Dinner",
            paymentMethod = "Card",
            description = "Pizza",
            account = "Bank",
            transferId = null
        )
        coEvery { dataSource.update(transaction) } returns Unit

        // Act
        repository.update(transaction)

        // Assert
        coVerify { dataSource.update(transaction) }
    }
}
