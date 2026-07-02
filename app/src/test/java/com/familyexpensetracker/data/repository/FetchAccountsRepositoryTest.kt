package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalAccountsDataSource
import com.familyexpensetracker.data.remote.FetchAccountsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchAccountsRepositoryTest {

    private val remoteDataSource = mockk<FetchAccountsDataSource>()
    private val localDataSource = mockk<LocalAccountsDataSource>()
    private lateinit var repository: FetchAccountsRepository

    @Before
    fun setUp() {
        repository = FetchAccountsRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun `fetch returns local accounts if available`() = runBlocking {
        val accounts = listOf("Savings")
        every { localDataSource.getAccounts() } returns accounts

        val result = repository.fetch()

        assertEquals(accounts, result)
        coVerify(exactly = 0) { remoteDataSource.fetch() }
    }

    @Test
    fun `fetch returns remote accounts if local not available`() = runBlocking<Unit> {
        val accounts = listOf("Cash")
        every { localDataSource.getAccounts() } returns null
        coEvery { remoteDataSource.fetch() } returns accounts
        every { localDataSource.saveAccounts(any()) } returns Unit

        val result = repository.fetch()

        assertEquals(accounts, result)
        coVerify { remoteDataSource.fetch() }
        verify { localDataSource.saveAccounts(accounts) }
    }
}

