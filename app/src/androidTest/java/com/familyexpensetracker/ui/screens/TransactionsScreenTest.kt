package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.ProgressBarRangeInfo

class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ExpenseViewModel>(relaxed = true)
    
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(value = false)
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val selectedDateRangeFlow = MutableStateFlow<DateRange>(DateRange.Day(Date()))

    @Before
    fun setUp() {
        every { viewModel.transactions } returns transactionsFlow
        every { viewModel.isLoading } returns isLoadingFlow
        every { viewModel.errorMessage } returns errorMessageFlow
        every { viewModel.selectedDateRange } returns selectedDateRangeFlow
        every { viewModel.expenseCategories } returns MutableStateFlow(emptyMap())
        every { viewModel.incomeCategories } returns MutableStateFlow(emptyMap())
        every { viewModel.accounts } returns MutableStateFlow(emptyList())
    }

    @Test
    fun transactionsScreen_showsEmptyState() {
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNodeWithText("No transactions loaded").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_showsLoading() {
        isLoadingFlow.value = true
        
        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        composeTestRule.onNode(hasProgressBar()).assertExists()
    }

    @Test
    fun transactionsScreen_showsTransactions() {
        val txn = Transaction(
            txnId = "1",
            date = "2026/06/28",
            amount = -100.0,
            category = "Food",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Milk",
            account = "Wallet"
        )
        transactionsFlow.value = listOf(txn)

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // TransactionItem shows "Food/Groceries"
        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
        // Account name should be visible in the item
        composeTestRule.onNodeWithText("Wallet", substring = true).assertIsDisplayed()
        
        // Check for Edit and Delete icons
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()

        // Total amount highlight shows "₹100.00" (from %.2f)
        // Match only the header which has .00
        composeTestRule.onNodeWithText("100.00", substring = true).assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_toggleViewMode() {
        val txn = Transaction(
            txnId = "1",
            date = "2026/06/28",
            amount = -100.0,
            category = "Food",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Milk",
            account = "Wallet"
        )
        transactionsFlow.value = listOf(txn)

        composeTestRule.setContent {
            TransactionsScreen(viewModel)
        }

        // Initially in Activity mode
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()

        // Switch to Grouped mode
        composeTestRule.onNodeWithText("Grouped").performClick()
        
        // In Grouped mode, we should see the account name first
        composeTestRule.onNodeWithText("Wallet").assertIsDisplayed()
    }

    private fun hasProgressBar(): SemanticsMatcher = SemanticsMatcher.expectValue(
        SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo.Indeterminate
    )
}
