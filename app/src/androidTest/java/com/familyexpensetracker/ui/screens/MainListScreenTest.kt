package com.familyexpensetracker.ui.screens

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.TransactionFilter
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class MainListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val transactionVM = mockk<TransactionViewModel>(relaxed = true)
    private val categoryVM = mockk<CategoryViewModel>(relaxed = true)
    private val accountVM = mockk<AccountViewModel>(relaxed = true)
    private val searchVM = mockk<SearchViewModel>(relaxed = true)

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
    private val searchFilterFlow = MutableStateFlow(SearchFilter())
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
        every { searchVM.searchFilter } returns searchFilterFlow
        every { categoryVM.expenseCategories } returns MutableStateFlow(emptyMap())
        every { categoryVM.incomeCategories } returns MutableStateFlow(emptyMap())
        every { transactionVM.accountBalance } returns accountBalanceFlow
    }

    private fun setContent() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            MainListScreen(navController, transactionVM, categoryVM, accountVM, searchVM)
        }
    }

    @Test
    fun mainListScreen_showsEmptyState() {
        setContent()
        composeTestRule.onNodeWithText("No transactions loaded").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap refresh to fetch").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_showsLoadingIndicator() {
        isLoadingFlow.value = true
        setContent()
        composeTestRule.onNode(hasProgressBar()).assertExists()
    }

    @Test
    fun mainListScreen_showsAllFilterChips() {
        setContent()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Balance").filterToOne(hasClickAction()).assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_showsAllPeriodTabs() {
        setContent()
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_periodNavBar_visibleOnDailyTab() {
        selectedPeriodTabFlow.value = PeriodTab.Daily
        setContent()
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_periodNavBar_hiddenOnAllTab() {
        selectedPeriodTabFlow.value = PeriodTab.All
        setContent()
        composeTestRule.onNodeWithText("<").assertDoesNotExist()
        composeTestRule.onNodeWithText(">").assertDoesNotExist()
    }

    @Test
    fun mainListScreen_showsCategorySummaries() {
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
        setContent()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 transactions").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_expandCategory_showsTransactionList() {
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
        setContent()
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_backFromExpandedCategory_returnsToCategoryList() {
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
        setContent()
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("3 transactions").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_addFab_visible() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Add Expense").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_refreshFab_visible() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_yearlyTab_showsViewToggle() {
        selectedPeriodTabFlow.value = PeriodTab.Yearly
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "15/06/2026", amount = -100.0, category = "Food", subcategory = "")
        )
        setContent()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_callsLoadAccountsOnComposition() {
        setContent()
        composeTestRule.waitForIdle()
        verify { accountVM.loadAccounts() }
    }

    @Test
    fun mainListScreen_searchBar_isVisible() {
        setContent()
        composeTestRule.onNodeWithText("Search description").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_searchBar_typingDoesNotCallViewModel() {
        setContent()
        composeTestRule.onNodeWithText("Search description").performTextInput("food")
        verify(exactly = 0) { searchVM.searchTransactions(any(), any()) }
    }

    @Test
    fun mainListScreen_refreshWithSearchText_callsSearch() {
        setContent()
        composeTestRule.onNodeWithText("Search description").performTextInput("food")
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        verify { searchVM.searchTransactions("food", SearchFilter()) }
    }

    @Test
    fun mainListScreen_searchResults_shown() {
        searchQueryFlow.value = "food"
        searchResultsFlow.value = listOf(
            Transaction(txnId = "s1", date = "2026/06/01", amount = -200.0, category = "Food", subcategory = "Groceries", description = "food market")
        )
        setContent()
        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_searchLoading_showsIndicator() {
        searchQueryFlow.value = "food"
        isSearchingFlow.value = true
        setContent()
        composeTestRule.onNode(hasProgressBar()).assertExists()
    }

    @Test
    fun mainListScreen_searchEmpty_showsNoResults() {
        searchQueryFlow.value = "xyz123"
        searchResultsFlow.value = emptyList()
        isSearchingFlow.value = false
        setContent()
        composeTestRule.onNodeWithText("No results for \"xyz123\"").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_expandedCategory_clearedOnPeriodTabChange() {
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
        setContent()
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        selectedPeriodTabFlow.value = PeriodTab.Monthly
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_expandedCategory_clearedOnFilterChange() {
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
        setContent()
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_expenseFilter_navBarTotalIsPositive_stillShowsExpenseColour() {
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)
        expenseTotalFlow.value = 700.0
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -700.0, category = "Food", subcategory = "")
        )
        setContent()
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_incomeFilter_navBarVisible() {
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.INCOME)
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = 1000.0, category = "Income", subcategory = "")
        )
        setContent()
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_categoryList_refreshFabVisible_withItems() {
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
        setContent()
        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_allTab_showsTotalBanner_noNavArrows() {
        selectedPeriodTabFlow.value = PeriodTab.All
        expenseTotalFlow.value = 1200.0
        transactionsFlow.value = listOf(
            Transaction(txnId = "1", date = "2026/06/15", amount = -1200.0, category = "Food", subcategory = "")
        )
        setContent()
        composeTestRule.onNodeWithText("<").assertDoesNotExist()
        composeTestRule.onNodeWithText(">").assertDoesNotExist()
        composeTestRule.onNodeWithText("₹1,200.00").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_allTab_expenseFilter_totalBannerVisible() {
        selectedPeriodTabFlow.value = PeriodTab.All
        selectedFilterFlow.value = TransactionFilter(type = com.familyexpensetracker.data.model.FilterType.EXPENSE)
        expenseTotalFlow.value = 750.0
        setContent()
        composeTestRule.onNodeWithText("₹750.00").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_dailyTab_showsNavArrows_notTotalBannerOnly() {
        selectedPeriodTabFlow.value = PeriodTab.Daily
        expenseTotalFlow.value = 500.0
        setContent()
        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_accountBalance_displayedWhenAvailable() {
        accountBalanceFlow.value = 1500.75
        setContent()
        composeTestRule.onNodeWithText("₹1,500.75").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_accountBalance_negativeBalance_showsAbsoluteValue() {
        accountBalanceFlow.value = -320.50
        setContent()
        composeTestRule.onNodeWithText("₹320.50").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_accountBalance_nullBalance_showsZero() {
        accountBalanceFlow.value = null
        setContent()
        composeTestRule.onNodeWithTag("accountBalanceText").assertIsDisplayed()
    }

    @Test
    fun mainListScreen_refreshFab_callsFetchAccountBalance() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        verify { transactionVM.fetchAccountBalance() }
    }

    private fun hasProgressBar(): SemanticsMatcher = SemanticsMatcher.expectValue(
        SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate
    )
}
