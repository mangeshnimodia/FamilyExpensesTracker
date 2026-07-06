package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.data.repository.AddTransactionRepository
import com.familyexpensetracker.data.repository.DeleteTransactionRepository
import com.familyexpensetracker.data.repository.FetchAccountBalanceRepository
import com.familyexpensetracker.data.repository.FetchTransactionsRepository
import com.familyexpensetracker.data.repository.UpdateTransactionRepository
import com.familyexpensetracker.ui.screens.PeriodTab
import com.familyexpensetracker.ui.screens.TransactionSummaryCalculator
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val fetchRepository = mockk<FetchTransactionsRepository>()
    private val addRepository = mockk<AddTransactionRepository>()
    private val deleteRepository = mockk<DeleteTransactionRepository>()
    private val updateRepository = mockk<UpdateTransactionRepository>()
    private val accountBalanceRepository = mockk<FetchAccountBalanceRepository>()
    private val summaryCalculator = TransactionSummaryCalculator()

    private lateinit var viewModel: TransactionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionViewModel(
            fetchRepository,
            addRepository,
            deleteRepository,
            updateRepository,
            accountBalanceRepository,
            summaryCalculator,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchTransactions updates state with data from repository`() = runTest {
        val transactions = listOf(mockk<Transaction>())
        coEvery { fetchRepository.fetch(any(), any()) } returns transactions

        viewModel.fetchTransactions()

        assertEquals(transactions, viewModel.transactions.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `fetchTransactions sets error message on failure`() = runTest {
        coEvery { fetchRepository.fetch(any(), any()) } throws Exception("Network Error")

        viewModel.fetchTransactions()

        assertEquals("Fetch failed: Network Error", viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `addTransaction calls repository and updates range`() = runTest {
        val transaction = mockk<Transaction>()
        val date = Date()
        coEvery { addRepository.add(any()) } returns mockk()

        viewModel.addTransaction(transaction, date)

        coVerify { addRepository.add(transaction) }
        assertTrue(viewModel.selectedDateRange.value is DateRange.Day)
        assertEquals(emptyList<Transaction>(), viewModel.transactions.value)
    }

    @Test
    fun `addAccountTransfer creates two entries with same transferId`() = runTest {
        val fromAccount = "Bank"
        val toAccount = "Cash"
        val amount = 100.0
        val paymentMethod = "UPI"
        val description = "Atm withdrawal"
        val date = Date()

        coEvery { addRepository.add(any(), any()) } returns mockk()

        viewModel.addAccountTransfer(fromAccount, toAccount, amount, paymentMethod, description, date)

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
        val transaction = mockk<Transaction>()
        val date = Date()
        coEvery { updateRepository.update(any()) } returns Unit

        viewModel.updateTransaction(transaction, date)

        coVerify { updateRepository.update(transaction) }
        assertTrue(viewModel.selectedDateRange.value is DateRange.Day)
        assertEquals(emptyList<Transaction>(), viewModel.transactions.value)
    }

    @Test
    fun `fetchTransactions cancels previous job if called again`() = runTest {
        val transactions1 = listOf(mockk<Transaction>(relaxed = true))
        val transactions2 = listOf(mockk<Transaction>(relaxed = true))

        coEvery { fetchRepository.fetch(any(), any()) } coAnswers {
            delay(1000.milliseconds)
            transactions1
        } andThenAnswer {
            transactions2
        }

        viewModel.fetchTransactions()
        advanceTimeBy(500)
        viewModel.fetchTransactions()
        advanceUntilIdle()

        assertEquals(transactions2, viewModel.transactions.value)
    }

    @Test
    fun `setSelectedFilter triggers fetch`() = runTest {
        val filter = TransactionFilter(selectedAccounts = listOf("Bank"))
        coEvery { fetchRepository.fetch(any(), any()) } returns emptyList()

        viewModel.setSelectedFilter(filter)
        viewModel.fetchTransactions()
        testScheduler.runCurrent()

        assertEquals(filter, viewModel.selectedFilter.value)
        coVerify { fetchRepository.fetch(any(), eq(filter)) }
    }

    @Test
    fun `selectedPeriodTab defaults to Daily`() {
        assertEquals(PeriodTab.Daily, viewModel.selectedPeriodTab.value)
    }

    @Test
    fun `setSelectedPeriodTab updates tab`() {
        viewModel.setSelectedPeriodTab(PeriodTab.Monthly)
        assertEquals(PeriodTab.Monthly, viewModel.selectedPeriodTab.value)
    }

    @Test
    fun `setSelectedPeriodTab is idempotent`() {
        viewModel.setSelectedPeriodTab(PeriodTab.Yearly)
        viewModel.setSelectedPeriodTab(PeriodTab.Yearly)
        assertEquals(PeriodTab.Yearly, viewModel.selectedPeriodTab.value)
    }

    @Test
    fun `selectedAccount defaults to Passbook`() {
        assertEquals("Passbook", viewModel.selectedAccount.value)
    }

    @Test
    fun `setSelectedAccount updates account and clears transactions`() {
        viewModel.setSelectedAccount("Bank")
        assertEquals("Bank", viewModel.selectedAccount.value)
        assertEquals(emptyList<Transaction>(), viewModel.transactions.value)
    }

    @Test
    fun `setSelectedAccount to null clears account`() {
        viewModel.setSelectedAccount("Bank")
        viewModel.setSelectedAccount(null)
        assertNull(viewModel.selectedAccount.value)
    }

    @Test
    fun `fetchTransactions populates categorySummaries`() = runTest {
        val txn = Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
        coEvery { fetchRepository.fetch(any(), any()) } returns listOf(txn)

        viewModel.fetchTransactions()

        assertEquals(1, viewModel.categorySummaries.value.size)
        assertEquals("Food", viewModel.categorySummaries.value[0].category)
    }

    @Test
    fun `fetchTransactions populates monthSummaries`() = runTest {
        val txn = Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
        coEvery { fetchRepository.fetch(any(), any()) } returns listOf(txn)

        viewModel.fetchTransactions()

        assertEquals(1, viewModel.monthSummaries.value.size)
        assertEquals(2026, viewModel.monthSummaries.value[0].year)
    }

    @Test
    fun `fetchTransactions clears summaries on empty result`() = runTest {
        coEvery { fetchRepository.fetch(any(), any()) } returns emptyList()

        viewModel.fetchTransactions()

        assertTrue(viewModel.categorySummaries.value.isEmpty())
        assertTrue(viewModel.monthSummaries.value.isEmpty())
    }

    @Test
    fun `expenseTotal defaults to 0`() {
        assertEquals(0.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `fetchTransactions sets expenseTotal to sum of negative amounts`() = runTest {
        val txns = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = ""),
            Transaction(txnId = "2", date = "2026/06/15", amount = -200.0, category = "Transport", subcategory = ""),
            Transaction(txnId = "3", date = "2026/06/15", amount = 1000.0, category = "Income", subcategory = ""),
        )
        coEvery { fetchRepository.fetch(any(), any()) } returns txns

        viewModel.fetchTransactions()

        assertEquals(-700.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `fetchTransactions sets expenseTotal to 0 when no expenses`() = runTest {
        val txns = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = 1000.0, category = "Income", subcategory = ""),
        )
        coEvery { fetchRepository.fetch(any(), any()) } returns txns

        viewModel.fetchTransactions()

        assertEquals(0.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `setSelectedDateRange clears expenseTotal`() = runTest {
        val txns = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )
        coEvery { fetchRepository.fetch(any(), any()) } returns txns
        viewModel.fetchTransactions()
        assertEquals(-500.0, viewModel.expenseTotal.value, 0.001)

        viewModel.setSelectedDateRange(DateRange.Month(2026, 5))

        assertEquals(0.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `setSelectedFilter clears expenseTotal`() = runTest {
        val txns = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )
        coEvery { fetchRepository.fetch(any(), any()) } returns txns
        viewModel.fetchTransactions()
        assertEquals(-500.0, viewModel.expenseTotal.value, 0.001)

        viewModel.setSelectedFilter(TransactionFilter(selectedAccounts = listOf("Bank")))

        assertEquals(0.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `setSelectedAccount clears expenseTotal`() = runTest {
        val txns = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )
        coEvery { fetchRepository.fetch(any(), any()) } returns txns
        viewModel.fetchTransactions()
        assertEquals(-500.0, viewModel.expenseTotal.value, 0.001)

        viewModel.setSelectedAccount("Bank")

        assertEquals(0.0, viewModel.expenseTotal.value, 0.001)
    }

    @Test
    fun `accountBalance defaults to null`() {
        assertNull(viewModel.accountBalance.value)
    }

    @Test
    fun `fetchAccountBalance sets accountBalance from repository`() = runTest {
        coEvery { accountBalanceRepository.fetchBalance(any()) } returns 1234.50
        viewModel.fetchAccountBalance()
        assertEquals(1234.50, viewModel.accountBalance.value!!, 0.001)
    }

    @Test
    fun `fetchAccountBalance uses current selectedAccount`() = runTest {
        coEvery { accountBalanceRepository.fetchBalance("Passbook") } returns 500.0
        viewModel.fetchAccountBalance()
        coVerify { accountBalanceRepository.fetchBalance("Passbook") }
    }

    @Test
    fun `setSelectedAccount clears accountBalance to null`() = runTest {
        coEvery { accountBalanceRepository.fetchBalance(any()) } returns 500.0
        viewModel.fetchAccountBalance()
        assertEquals(500.0, viewModel.accountBalance.value!!, 0.001)

        viewModel.setSelectedAccount("Bank")

        assertNull(viewModel.accountBalance.value)
    }

    @Test
    fun `fetchAccountBalance on error leaves balance as null`() = runTest {
        coEvery { accountBalanceRepository.fetchBalance(any()) } throws Exception("Network failure")
        viewModel.fetchAccountBalance()
        assertNull(viewModel.accountBalance.value)
    }

    @Test
    fun `setSelectedDateRange updates selectedDateRange value`() = runTest {
        val newDate = Calendar.getInstance().apply { set(2026, 5, 15) }.time
        viewModel.setSelectedDateRange(DateRange.Day(newDate))
        val range = viewModel.selectedDateRange.value
        assertTrue(range is DateRange.Day)
        assertEquals(newDate, (range as DateRange.Day).date)
    }

    @Test
    fun `fetchTransactions after setSelectedDateRange uses new range`() = runTest {
        val newDate = Calendar.getInstance().apply { set(2026, 5, 15) }.time
        val newRange = DateRange.Day(newDate)
        coEvery { fetchRepository.fetch(any(), any()) } returns emptyList()
        viewModel.setSelectedDateRange(newRange)
        viewModel.fetchTransactions()
        coVerify { fetchRepository.fetch(newRange, any()) }
    }
}
