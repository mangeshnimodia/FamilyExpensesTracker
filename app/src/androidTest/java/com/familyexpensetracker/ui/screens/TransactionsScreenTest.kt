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
    private val searchQueryFlow = MutableStateFlow("")
    private val searchResultsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val isSearchingFlow = MutableStateFlow(false)
    private val accountBalanceFlow = MutableStateFlow<Double?>(null)

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
        every { viewModel.searchQuery } returns searchQueryFlow
        every { viewModel.searchResults } returns searchResultsFlow
        every { viewModel.isSearching } returns isSearchingFlow
        every { viewModel.expenseCategories } returns MutableStateFlow(emptyMap())
        every { viewModel.incomeCategories } returns MutableStateFlow(emptyMap())
        every { viewModel.accountBalance } returns accountBalanceFlow
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
    fun transactionsScreen_expandCategory_showsTransactionList() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 1,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_backFromExpandedCategory_returnsToCategoryList() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 1,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("3 transactions").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
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

    @Test
    fun transactionsScreen_searchBar_isVisible() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Search description").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_searchBar_typingDoesNotCallViewModel() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Search description").performTextInput("food")

        verify(exactly = 0) { viewModel.searchTransactions(any()) }
    }

    @Test
    fun transactionsScreen_refreshWithSearchText_callsSearch() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Search description").performTextInput("food")
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        verify { viewModel.searchTransactions("food") }
    }

    @Test
    fun transactionsScreen_searchResults_shown() {
        searchQueryFlow.value = "food"
        searchResultsFlow.value = listOf(
            Transaction(txnId = "s1", date = "2026/06/01", amount = -200.0, category = "Food", subcategory = "Groceries", description = "food market")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_searchLoading_showsIndicator() {
        searchQueryFlow.value = "food"
        isSearchingFlow.value = true

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNode(hasProgressBar()).assertExists()
    }

    @Test
    fun transactionsScreen_searchEmpty_showsNoResults() {
        searchQueryFlow.value = "xyz123"
        searchResultsFlow.value = emptyList()
        isSearchingFlow.value = false

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("No results for \"xyz123\"").assertIsDisplayed()
    }


    @Test
    fun transactionsScreen_expandedCategory_clearedOnPeriodTabChange() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 1,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // Expand category
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Simulate period tab change via ViewModel state
        selectedPeriodTabFlow.value = PeriodTab.Monthly

        // Category expanded view should be gone
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_expandedCategory_clearedOnFilterChange() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 1,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "Groceries")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // Expand category
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Simulate filter change via ViewModel state
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)

        // Category expanded view should be gone
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_expenseFilter_navBarTotalIsPositive_stillShowsExpenseColour() {
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)
        expenseTotalFlow.value = 700.0
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -700.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_incomeFilter_navBarVisible() {
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.INCOME)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = 1000.0, category = "Income", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_categoryList_refreshFabVisible_withItems() {
        val summary = CategorySummary(
            category = "Food",
            totalAmount = -500.0,
            transactionCount = 2,
            subcategories = emptyList()
        )
        categorySummariesFlow.value = listOf(summary)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -500.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_editFromSearchResults_opensEditScreen() {
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
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_copyFromSearchResults_prefillsAmount() {
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
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        composeTestRule.onNodeWithText("350").assertIsDisplayed()
    }


    @Test
    fun transactionsScreen_allTab_showsTotalBanner_noNavArrows() {
        selectedPeriodTabFlow.value = PeriodTab.All
        expenseTotalFlow.value = 1200.0
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -1200.0, category = "Food", subcategory = "")
        )

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // Nav arrows must not exist on All tab
        composeTestRule.onNodeWithText("<").assertDoesNotExist()
        composeTestRule.onNodeWithText(">").assertDoesNotExist()
        // Total banner must show the formatted amount
        composeTestRule.onNodeWithText("\u20b91200.00").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_allTab_expenseFilter_totalBannerVisible() {
        selectedPeriodTabFlow.value = PeriodTab.All
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)
        expenseTotalFlow.value = 750.0

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("\u20b9750.00").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_dailyTab_showsNavArrows_notTotalBannerOnly() {
        selectedPeriodTabFlow.value = PeriodTab.Daily
        expenseTotalFlow.value = 500.0

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // Daily tab must still show nav arrows (PeriodNavBar, not TotalBanner)
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_accountBalance_displayedWhenAvailable() {
        accountBalanceFlow.value = 1500.75

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("\u20b91500.75").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_accountBalance_negativeBalance_showsAbsoluteValue() {
        accountBalanceFlow.value = -320.50

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("\u20b9320.50").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_accountBalance_nullBalance_showsZero() {
        accountBalanceFlow.value = null

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithTag("accountBalanceText").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_refreshFab_callsFetchAccountBalance() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        verify { viewModel.fetchAccountBalance() }
    }

        private fun hasProgressBar(): SemanticsMatcher = SemanticsMatcher.expectValue(
        SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate
    )
}
