package com.familyexpensetracker.ui.screens

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ExpenseViewModel>(relaxed = true)

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

    @Before
    fun setUp() {
        every { viewModel.transactions } returns transactionsFlow
        every { viewModel.isLoading } returns isLoadingFlow
        every { viewModel.errorMessage } returns errorMessageFlow
        every { viewModel.selectedDateRange } returns selectedDateRangeFlow
        every { viewModel.selectedFilter } returns selectedFilterFlow
        every { viewModel.accounts } returns accountsFlow
        every { viewModel.selectedPeriodTab } returns selectedPeriodTabFlow
        every { viewModel.selectedAccount } returns selectedAccountFlow
        every { viewModel.categorySummaries } returns categorySummariesFlow
        every { viewModel.monthSummaries } returns monthSummariesFlow
        every { viewModel.expenseTotal } returns expenseTotalFlow
        every { viewModel.expenseCategories } returns MutableStateFlow(emptyMap())
        every { viewModel.incomeCategories } returns MutableStateFlow(emptyMap())
    }

    @Test
    fun transactionsScreen_showsEmptyState() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("No transactions loaded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap refresh to fetch").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_showsLoadingIndicator() {
        isLoadingFlow.value = true

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNode(hasProgressBar()).assertExists()
    }

    @Test
    fun transactionsScreen_showsAllFilterChips() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Balance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_showsAllPeriodTabs() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_periodNavBar_visibleOnDailyTab() {
        selectedPeriodTabFlow.value = PeriodTab.Daily

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // PeriodNavBar shows < and > navigation buttons
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_periodNavBar_hiddenOnAllTab() {
        selectedPeriodTabFlow.value = PeriodTab.All

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("<").assertDoesNotExist()
        composeTestRule.onNodeWithText(">").assertDoesNotExist()
    }

    @Test
    fun transactionsScreen_showsCategorySummaries() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 3,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 transactions").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_addFab_visible() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Add Expense").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_refreshFab_visible() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_topBar_showsTitle() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Family Expenses").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_yearlyTab_showsViewToggle() {
        selectedPeriodTabFlow.value = PeriodTab.Yearly
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "15/06/2026", amount = -100.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_callsLoadAccountsOnComposition() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.waitForIdle()

        verify { viewModel.loadAccounts() }
    }

    private fun hasProgressBar(): SemanticsMatcher = SemanticsMatcher.expectValue(
        SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate
    )
}
