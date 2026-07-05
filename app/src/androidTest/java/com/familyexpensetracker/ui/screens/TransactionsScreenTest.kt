package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.PaymentMethodViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val transactionVM = mockk<TransactionViewModel>(relaxed = true)
    private val categoryVM = mockk<CategoryViewModel>(relaxed = true)
    private val accountVM = mockk<AccountViewModel>(relaxed = true)
    private val searchVM = mockk<SearchViewModel>(relaxed = true)
    private val paymentMethodVM = mockk<PaymentMethodViewModel>(relaxed = true)

    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val selectedDateRangeFlow = MutableStateFlow<DateRange>(DateRange.Day(Date()))
    private val selectedFilterFlow = MutableStateFlow(TransactionFilter())
    private val accountsFlow = MutableStateFlow<List<String>>(emptyList())
    private val selectedPeriodTabFlow = MutableStateFlow(PeriodTab.Daily)
    private val selectedAccountFlow = MutableStateFlow<String?>("Passbook")
    private val categorySummariesFlow = MutableStateFlow<List<CategorySummary>>(emptyList())
    private val monthSummariesFlow = MutableStateFlow<List<MonthSummary>>(emptyList())
    private val expenseTotalFlow = MutableStateFlow(0.0)
    private val searchQueryFlow = MutableStateFlow("")
    private val searchResultsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val isSearchingFlow = MutableStateFlow(false)
    private val paymentMethodsFlow = MutableStateFlow<List<String>>(listOf("Cash", "UPI", "Card"))
    private val accountBalanceFlow = MutableStateFlow<Double?>(null)

    @Before
    fun setUp() {
        every { transactionVM.transactions } returns transactionsFlow
        every { transactionVM.isLoading } returns isLoadingFlow
        every { transactionVM.errorMessage } returns errorMessageFlow
        every { transactionVM.selectedDateRange } returns selectedDateRangeFlow
        every { transactionVM.selectedFilter } returns selectedFilterFlow
        every { accountVM.accounts } returns accountsFlow
        every { transactionVM.selectedPeriodTab } returns selectedPeriodTabFlow
        every { transactionVM.selectedAccount } returns selectedAccountFlow
        every { transactionVM.categorySummaries } returns categorySummariesFlow
        every { transactionVM.monthSummaries } returns monthSummariesFlow
        every { transactionVM.expenseTotal } returns expenseTotalFlow
        every { searchVM.searchQuery } returns searchQueryFlow
        every { searchVM.searchResults } returns searchResultsFlow
        every { searchVM.isSearching } returns isSearchingFlow
        every { categoryVM.expenseCategories } returns MutableStateFlow(emptyMap())
        every { categoryVM.incomeCategories } returns MutableStateFlow(emptyMap())
        every { transactionVM.accountBalance } returns accountBalanceFlow
        every { paymentMethodVM.paymentMethods } returns paymentMethodsFlow
    }

    @Test
    fun transactionsScreen_navigatesToAddScreen_onFabClick() {
        composeTestRule.setContent {
            TransactionsScreen(transactionVM, categoryVM, accountVM, searchVM, paymentMethodVM)
        }

        composeTestRule.onNodeWithContentDescription("Add Expense").performClick()

        composeTestRule.onNodeWithText("Add Entry").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_navigatesToEditScreen_fromMainList() {
        val txn = Transaction(
            txnId = "edit-1",
            date = "2026/06/01",
            amount = -200.0,
            category = "Food",
            subcategory = "Groceries",
            description = "food market"
        )
        transactionsFlow.value = listOf(txn)
        categorySummariesFlow.value = listOf(
            CategorySummary(category = "Food", totalAmount = -200.0, transactionCount = 1, subcategories = emptyList())
        )

        composeTestRule.setContent {
            TransactionsScreen(transactionVM, categoryVM, accountVM, searchVM, paymentMethodVM)
        }

        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_navigatesToEditScreen_fromSearchResults() {
        val txn = Transaction(
            txnId = "search-1",
            date = "2026/06/01",
            amount = -200.0,
            category = "Food",
            subcategory = "Groceries",
            description = "food market"
        )
        searchQueryFlow.value = "food"
        searchResultsFlow.value = listOf(txn)
        transactionsFlow.value = emptyList()

        composeTestRule.setContent {
            TransactionsScreen(transactionVM, categoryVM, accountVM, searchVM, paymentMethodVM)
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_navigatesToCopyScreen_fromSearchResults() {
        val txn = Transaction(
            txnId = "search-2",
            date = "2026/06/01",
            amount = -350.0,
            category = "Transport",
            subcategory = "Fuel",
            description = "petrol"
        )
        searchQueryFlow.value = "petrol"
        searchResultsFlow.value = listOf(txn)
        transactionsFlow.value = emptyList()

        composeTestRule.setContent {
            TransactionsScreen(transactionVM, categoryVM, accountVM, searchVM, paymentMethodVM)
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        composeTestRule.onNodeWithText("350").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_addScreen_dismissReturnsToMainList() {
        composeTestRule.setContent {
            TransactionsScreen(transactionVM, categoryVM, accountVM, searchVM, paymentMethodVM)
        }

        composeTestRule.onNodeWithContentDescription("Add Expense").performClick()
        composeTestRule.onNodeWithText("Add Entry").assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Search description").assertIsDisplayed()
    }
}
