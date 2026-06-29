package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class AddExpenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ExpenseViewModel>(relaxed = true)
    
    private val expenseCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val incomeCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val accountsFlow = MutableStateFlow<List<String>>(emptyList())
    private val selectedDateRangeFlow = MutableStateFlow<DateRange>(DateRange.Day(Date()))

    @Before
    fun setUp() {
        every { viewModel.expenseCategories } returns expenseCategoriesFlow
        every { viewModel.incomeCategories } returns incomeCategoriesFlow
        every { viewModel.accounts } returns accountsFlow
        every { viewModel.selectedDateRange } returns selectedDateRangeFlow
        
        // Provide some default data so that dropdowns have options if needed
        expenseCategoriesFlow.value = mapOf("Daily Living" to listOf("Groceries"))
        incomeCategoriesFlow.value = mapOf("Income" to listOf("Salary"))
        accountsFlow.value = listOf("Passbook")
    }

    @Test
    fun addExpenseScreen_initialState() {
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").assertIsDisplayed()
        // The title is in the TopAppBar, the button is at the bottom.
        // Button usually has a click action and is focusable.
        composeTestRule.onAllNodesWithText("Add Expense").filterToOne(hasClickAction()).assertExists()
    }

    @Test
    fun addExpenseScreen_canSwitchToIncome() {
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {})
        }

        // Tab "Income" - use filterToOne to avoid ambiguity with the button that will soon have this text
        composeTestRule.onAllNodesWithText("Income").filterToOne(hasClickAction()).performClick()
        // Button text should change to "Add Income"
        composeTestRule.onNode(hasText("Add Income") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_validatesInputBeforeAdding() {
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {})
        }

        // Add button should be disabled initially (amount is empty)
        composeTestRule.onAllNodesWithText("Add Expense").filterToOne(hasClickAction()).assertIsNotEnabled()

        // Enter amount
        composeTestRule.onNodeWithText("Amount").performTextInput("50")
        
        // Should now be enabled
        composeTestRule.onAllNodesWithText("Add Expense").filterToOne(hasClickAction()).assertIsEnabled()
    }

    @Test
    fun addExpenseScreen_triggersViewModelOnAdd() {
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onAllNodesWithText("Add Expense").filterToOne(hasClickAction()).performClick()

        verify { viewModel.addTransaction(any(), any()) }
    }

    @Test
    fun addExpenseScreen_initialStateForEdit() {
        val txn = com.familyexpensetracker.data.model.Transaction(
            txnId = "edit-1",
            date = "2026/06/28",
            amount = -250.0,
            category = "Daily Living",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Test Edit",
            account = "Passbook"
        )
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Edit Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("250").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Edit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_triggersViewModelOnUpdate() {
        val txn = com.familyexpensetracker.data.model.Transaction(
            txnId = "edit-1",
            date = "2026/06/28",
            amount = -250.0,
            category = "Daily Living",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Test Edit",
            account = "Passbook"
        )
        composeTestRule.setContent {
            AddExpenseScreen(viewModel = viewModel, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Update").performClick()

        verify { viewModel.updateTransaction(any(), any()) }
    }
}

