package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.repository.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val fetchRepository = mockk<FetchTransactionsRepository>()
    private val addRepository = mockk<AddTransactionRepository>()
    private val deleteRepository = mockk<DeleteTransactionRepository>()
    private val expenseCategoriesRepository = mockk<FetchCategoriesRepository>()
    private val incomeCategoriesRepository = mockk<FetchCategoriesRepository>()
    private val accountsRepository = mockk<FetchAccountsRepository>()

    private lateinit var viewModel: ExpenseViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ExpenseViewModel(
            fetchRepository,
            addRepository,
            deleteRepository,
            expenseCategoriesRepository,
            incomeCategoriesRepository,
            accountsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchTransactions updates state with data from repository`() = runTest {
        // Arrange
        val transactions = listOf(mockk<Transaction>())
        coEvery { fetchRepository.fetch(any()) } returns transactions

        // Act
        viewModel.fetchTransactions()

        // Assert
        assertEquals(transactions, viewModel.transactions.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `fetchTransactions sets error message on failure`() = runTest {
        // Arrange
        coEvery { fetchRepository.fetch(any()) } throws Exception("Network Error")

        // Act
        viewModel.fetchTransactions()

        // Assert
        assertEquals("Fetch failed: Network Error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `addTransaction calls repository and updates range`() = runTest {
        // Arrange
        val transaction = mockk<Transaction>()
        val date = Date()
        coEvery { addRepository.add(any()) } returns mockk()

        // Act
        viewModel.addTransaction(transaction, date)

        // Assert
        coVerify { addRepository.add(transaction) }
        assertTrue(viewModel.selectedDateRange.value is DateRange.Day)
        assertEquals(emptyList<Transaction>(), viewModel.transactions.value)
    }

    @Test
    fun `loadCategories updates both expense and income categories`() = runTest {
        // Arrange
        val expenses = mapOf("Food" to listOf("Groceries"))
        val income = mapOf("Job" to listOf("Salary"))
        coEvery { expenseCategoriesRepository.fetch() } returns expenses
        coEvery { incomeCategoriesRepository.fetch() } returns income

        // Act
        viewModel.loadCategories()

        // Assert
        assertEquals(expenses, viewModel.expenseCategories.value)
        assertEquals(income, viewModel.incomeCategories.value)
    }

    @Test
    fun `loadAccounts updates accounts state`() = runTest {
        // Arrange
        val accounts = listOf("Bank", "Cash")
        coEvery { accountsRepository.fetch() } returns accounts

        // Act
        viewModel.loadAccounts()

        // Assert
        assertEquals(accounts, viewModel.accounts.value)
    }
}
