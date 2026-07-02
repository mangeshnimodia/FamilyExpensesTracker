package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.remote.FetchTransactionsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchAccountBalanceRepositoryTest {

    private val dataSource = mockk<FetchTransactionsDataSource>()
    private lateinit var repository: FetchAccountBalanceRepository

    @Before
    fun setUp() {
        repository = FetchAccountBalanceRepository(dataSource)
    }

    @Test
    fun `fetchBalance returns net sum of all transactions for account`() = runTest {
        coEvery { dataSource.fetch(DateRange.All, any()) } returns listOf(
            Transaction(txnId = "1", date = "2026/01/01", amount = 1000.0, category = "Income", subcategory = ""),
            Transaction(txnId = "2", date = "2026/01/02", amount = -300.0, category = "Food", subcategory = ""),
        )
        val result = repository.fetchBalance("Passbook")
        assertEquals(700.0, result, 0.001)
    }

    @Test
    fun `fetchBalance calls dataSource with DateRange_All`() = runTest {
        coEvery { dataSource.fetch(DateRange.All, any()) } returns emptyList()
        repository.fetchBalance("Passbook")
        coVerify { dataSource.fetch(DateRange.All, any()) }
    }

    @Test
    fun `fetchBalance calls dataSource with BALANCE filter type`() = runTest {
        coEvery { dataSource.fetch(any(), any()) } returns emptyList()
        repository.fetchBalance("Passbook")
        coVerify { dataSource.fetch(any(), match { it.type == FilterType.BALANCE }) }
    }

    @Test
    fun `fetchBalance passes account name as selectedAccounts`() = runTest {
        coEvery { dataSource.fetch(any(), any()) } returns emptyList()
        repository.fetchBalance("Savings")
        coVerify { dataSource.fetch(any(), match { it.selectedAccounts == listOf("Savings") }) }
    }

    @Test
    fun `fetchBalance with null account uses empty selectedAccounts`() = runTest {
        coEvery { dataSource.fetch(any(), any()) } returns emptyList()
        repository.fetchBalance(null)
        coVerify { dataSource.fetch(any(), match { it.selectedAccounts.isEmpty() }) }
    }

    @Test
    fun `fetchBalance returns 0 for empty result`() = runTest {
        coEvery { dataSource.fetch(any(), any()) } returns emptyList()
        val result = repository.fetchBalance("Passbook")
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `fetchBalance returns negative balance when expenses exceed income`() = runTest {
        coEvery { dataSource.fetch(any(), any()) } returns listOf(
            Transaction(txnId = "1", date = "2026/01/01", amount = 500.0, category = "Income", subcategory = ""),
            Transaction(txnId = "2", date = "2026/01/02", amount = -800.0, category = "Food", subcategory = ""),
        )
        val result = repository.fetchBalance("Passbook")
        assertEquals(-300.0, result, 0.001)
    }
}
