package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
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

    private val transactionVM = mockk<TransactionViewModel>(relaxed = true)
    private val categoryVM = mockk<CategoryViewModel>(relaxed = true)
    private val accountVM = mockk<AccountViewModel>(relaxed = true)

    private val expenseCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val incomeCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val accountsFlow = MutableStateFlow<List<String>>(emptyList())
    private val selectedDateRangeFlow = MutableStateFlow<DateRange>(DateRange.Day(Date()))

    @Before
    fun setUp() {
        every { categoryVM.expenseCategories } returns expenseCategoriesFlow
        every { categoryVM.incomeCategories } returns incomeCategoriesFlow
        every { accountVM.accounts } returns accountsFlow
        every { transactionVM.selectedDateRange } returns selectedDateRangeFlow
        
        // Provide some default data so that dropdowns have options if needed
        expenseCategoriesFlow.value = mapOf("Daily Living" to listOf("Groceries"))
        incomeCategoriesFlow.value = mapOf("Income" to listOf("Salary"))
        accountsFlow.value = listOf("Passbook")
    }

    @Test
    fun addExpenseScreen_initialState() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").assertIsDisplayed()
        // The title is in the TopAppBar, the button is at the bottom.
        // Button usually has a click action and is focusable.
        composeTestRule.onNode(hasText("Add Expense") and hasClickAction()).assertExists()
    }

    @Test
    fun addExpenseScreen_canSwitchToIncome() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        // Tab "Income" - use specific matcher to avoid ambiguity with the button
        composeTestRule.onNode(hasText("Income") and hasClickAction() and !hasText("Add Income")).performClick()
        // Button text should change to "Add Income"
        composeTestRule.onNode(hasText("Add Income") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_validatesInputBeforeAdding() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        // Add button should be disabled initially (amount is empty)
        composeTestRule.onNode(hasText("Add Expense") and hasClickAction()).assertIsNotEnabled()

        // Enter amount
        composeTestRule.onNodeWithText("Amount").performTextInput("50")
        
        // Should now be enabled
        composeTestRule.onNode(hasText("Add Expense") and hasClickAction()).assertIsEnabled()
    }

    @Test
    fun addExpenseScreen_triggersViewModelOnAdd() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onNode(hasText("Add Expense") and hasClickAction()).performClick()

        verify { transactionVM.addTransaction(any(), any()) }
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Edit Entry").assertIsDisplayed()
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Update").performClick()

        verify { transactionVM.updateTransaction(any(), any()) }
    }

    @Test
    fun addExpenseScreen_transferMode_showsToFromAccounts() {
        accountsFlow.value = listOf("Bank", "Cash")
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        // Switch to Transfer tab
        composeTestRule.onNodeWithText("Transfer").performClick()

        // Check for specific labels
        composeTestRule.onNodeWithText("From Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("To Account").assertIsDisplayed()
        
        // Category and Subcategory should NOT be displayed
        composeTestRule.onNodeWithText("Category").assertDoesNotExist()
        composeTestRule.onNodeWithText("Subcategory").assertDoesNotExist()
    }

    @Test
    fun addExpenseScreen_transferMode_triggersViewModel() {
        accountsFlow.value = listOf("Bank", "Cash")
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Transfer").performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("500")
        
        // Use dropdown logic to select accounts if needed, but here we just verify button click
        // Default accounts might already be set by the screen's remember state
        
        composeTestRule.onNode(hasText("Add Transfer") and hasClickAction()).performClick()

        verify { 
            transactionVM.addAccountTransfer(
                fromAccount = any(),
                toAccount = any(),
                amount = 500.0,
                paymentMethod = any(),
                description = any(),
                transactionDate = any()
            ) 
        }
    }

    @Test
    fun addExpenseScreen_copyMode_showsCopyButtonAndTitle() {
        val txn = com.familyexpensetracker.data.model.Transaction(
            txnId = "copy-src",
            date = "2026/06/28",
            amount = -300.0,
            category = "Daily Living",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Monthly groceries",
            account = "Passbook"
        )
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {}, transactionToEdit = txn, isCopy = true)
        }

        composeTestRule.onNodeWithText("Copy Entry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Copy").assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_copyMode_triggersAddNotUpdate() {
        val txn = com.familyexpensetracker.data.model.Transaction(
            txnId = "copy-src",
            date = "2026/06/28",
            amount = -300.0,
            category = "Daily Living",
            subcategory = "Groceries",
            paymentMethod = "Cash",
            description = "Monthly groceries",
            account = "Passbook"
        )
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, onDismiss = {}, transactionToEdit = txn, isCopy = true)
        }

        composeTestRule.onNodeWithText("Copy").performClick()

        verify { transactionVM.addTransaction(any(), any()) }
    }
}
