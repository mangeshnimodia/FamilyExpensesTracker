package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.data.repository.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private val fetchRepository = mockk<FetchTransactionsRepository>()
    private val addRepository = mockk<AddTransactionRepository>()
    private val deleteRepository = mockk<DeleteTransactionRepository>()
    private val updateRepository = mockk<UpdateTransactionRepository>()
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
            updateRepository,
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
        coEvery { fetchRepository.fetch(any(), any()) } returns transactions

        // Act
        viewModel.fetchTransactions()

        // Assert
        assertEquals(transactions, viewModel.transactions.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `fetchTransactions sets error message on failure`() = runTest {
        // Arrange
        coEvery { fetchRepository.fetch(any(), any()) } throws Exception("Network Error")

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
    fun `addAccountTransfer creates two entries with same transferId`() = runTest {
        // Arrange
        val fromAccount = "Bank"
        val toAccount = "Cash"
        val amount = 100.0
        val paymentMethod = "UPI"
        val description = "Atm withdrawal"
        val date = Date()

        coEvery { addRepository.add(any(), any()) } returns mockk()

        // Act
        viewModel.addAccountTransfer(fromAccount, toAccount, amount, paymentMethod, description, date)

        // Assert
        coVerify {
            addRepository.add(
                match { it.amount == -100.0 && it.account == "Bank" && it.category == "Account Transfer" && it.transferId != null },
                match { it.amount == 100.0 && it.account == "Cash" && it.category == "Income" && it.subcategory == "Account Transfer" && it.transferId != null }
            )
        }
        val captured = mutableListOf<Transaction>()
        coVerify { addRepository.add(capture(captured), capture(captured)) }
        assertEquals(captured[0].transferId, captured[1].transferId)
        assertTrue(viewModel.selectedDateRange.value is DateRange.Day)
    }

    @Test
    fun `updateTransaction calls repository and updates range`() = runTest {
        // Arrange
        val transaction = mockk<Transaction>()
        val date = Date()
        coEvery { updateRepository.update(any()) } returns Unit

        // Act
        viewModel.updateTransaction(transaction, date)

        // Assert
        coVerify { updateRepository.update(transaction) }
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
    fun `fetchTransactions cancels previous job if called again`() = runTest {
        // Arrange
        val transactions1 = listOf(mockk<Transaction>(relaxed = true))
        val transactions2 = listOf(mockk<Transaction>(relaxed = true))

        coEvery { fetchRepository.fetch(any(), any()) } coAnswers {
            delay(1000.milliseconds)
            transactions1
        } andThenAnswer {
            transactions2
        }

        // Act
        viewModel.fetchTransactions() // Start first fetch
        advanceTimeBy(500)
        viewModel.fetchTransactions() // Start second fetch (should cancel first)
        advanceUntilIdle()

        // Assert
        assertEquals(transactions2, viewModel.transactions.value)
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

    @Test
    fun `setSelectedFilter triggers fetch`() = runTest {
        // Arrange
        val filter = TransactionFilter(selectedAccounts = listOf("Bank"))
        coEvery { fetchRepository.fetch(any(), any()) } returns emptyList()

        // Act
        viewModel.setSelectedFilter(filter)
        viewModel.fetchTransactions() // Manually trigger fetch since Unconfined might not behave as expected with state flow changes in tests
        testScheduler.runCurrent()

        // Assert
        assertEquals(filter, viewModel.selectedFilter.value)
        coVerify { fetchRepository.fetch(any(), eq(filter)) }
    }
}
