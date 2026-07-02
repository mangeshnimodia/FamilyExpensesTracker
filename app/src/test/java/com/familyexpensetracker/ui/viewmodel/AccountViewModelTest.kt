package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.repository.FetchAccountsRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val accountsRepository = mockk<FetchAccountsRepository>()

    private lateinit var viewModel: AccountViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AccountViewModel(accountsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `accounts defaults to empty list`() {
        assertTrue(viewModel.accounts.value.isEmpty())
    }

    @Test
    fun `loadAccounts updates accounts state`() = runTest {
        val accounts = listOf("Bank", "Cash")
        coEvery { accountsRepository.fetch() } returns accounts

        viewModel.loadAccounts()

        assertEquals(accounts, viewModel.accounts.value)
    }

    @Test
    fun `loadAccounts on error leaves accounts unchanged`() = runTest {
        coEvery { accountsRepository.fetch() } throws Exception("Network error")

        viewModel.loadAccounts()

        assertTrue(viewModel.accounts.value.isEmpty())
    }

    @Test
    fun `loadAccounts is idempotent when called twice`() = runTest {
        val accounts = listOf("Bank", "Cash")
        coEvery { accountsRepository.fetch() } returns accounts

        viewModel.loadAccounts()
        viewModel.loadAccounts()

        assertEquals(accounts, viewModel.accounts.value)
    }
}
