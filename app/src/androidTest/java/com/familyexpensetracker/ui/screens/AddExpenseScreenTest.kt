package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.PaymentMethodViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.familyexpensetracker.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val transactionVM = mockk<TransactionViewModel>(relaxed = true)
    private val categoryVM = mockk<CategoryViewModel>(relaxed = true)
    private val accountVM = mockk<AccountViewModel>(relaxed = true)
    private val paymentMethodVM = mockk<PaymentMethodViewModel>(relaxed = true)

    private val expenseCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val incomeCategoriesFlow = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val accountsFlow = MutableStateFlow<List<String>>(emptyList())
    private val paymentMethodsFlow = MutableStateFlow<List<String>>(listOf("Cash", "UPI", "Card"))
    private val selectedDateRangeFlow = MutableStateFlow<DateRange>(DateRange.Day(Date()))

    @Before
    fun setUp() {
        every { categoryVM.expenseCategories } returns expenseCategoriesFlow
        every { categoryVM.incomeCategories } returns incomeCategoriesFlow
        every { accountVM.accounts } returns accountsFlow
        every { paymentMethodVM.paymentMethods } returns paymentMethodsFlow
        every { transactionVM.selectedDateRange } returns selectedDateRangeFlow

        // Provide some default data so that dropdowns have options if needed
        expenseCategoriesFlow.value = mapOf("Daily Living" to listOf("Groceries"))
        incomeCategoriesFlow.value = mapOf("Income" to listOf("Salary"))
        accountsFlow.value = listOf("Passbook")
    }

    @Test
    fun addExpenseScreen_initialState() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").assertIsDisplayed()
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertExists()
    }

    @Test
    fun addExpenseScreen_canSwitchToIncome() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Income") and hasClickAction()).performClick()
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_validatesInputBeforeAdding() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        // Add button should be disabled initially (amount is empty)
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsNotEnabled()

        composeTestRule.onNodeWithText("Amount").performTextInput("50")

        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsEnabled()
    }

    @Test
    fun addExpenseScreen_triggersViewModelOnAdd() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).performClick()

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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn)
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Update").performClick()

        verify { transactionVM.updateTransaction(any(), any()) }
    }

    @Test
    fun addExpenseScreen_transferMode_showsToFromAccounts() {
        accountsFlow.value = listOf("Bank", "Cash")
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Transfer").performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("500")

        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).performClick()

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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn, isCopy = true)
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn, isCopy = true)
        }

        composeTestRule.onNodeWithText("Copy").performClick()

        verify { transactionVM.addTransaction(any(), any()) }
    }
    @Test
    fun addExpenseScreen_dateButton_showsUiFormat() {
        val fixedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        selectedDateRangeFlow.value = DateRange.Day(fixedDate)
        val expectedLabel = SimpleDateFormat(AppConstants.DATE_FORMAT_UI, Locale.getDefault()).format(fixedDate)

        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(expectedLabel).assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_dateButton_doesNotShowDbFormat() {
        val fixedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        selectedDateRangeFlow.value = DateRange.Day(fixedDate)

        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        // DB format "2026/06/15" must never appear on screen
        composeTestRule.onNodeWithText("2026/06/15").assertDoesNotExist()
    }

    // ── Add + Add more dual buttons ──────────────────────────────────────────

    @Test
    fun addExpenseScreen_showsAddMoreButton_forNewEntry() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Add More") and hasClickAction()).assertExists()
    }

    @Test
    fun addExpenseScreen_doesNotShowAddMoreButton_forEditEntry() {
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn)
        }

        composeTestRule.onNodeWithText("Add More").assertDoesNotExist()
    }

    @Test
    fun addExpenseScreen_doesNotShowAddMoreButton_forCopyEntry() {
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {}, transactionToEdit = txn, isCopy = true)
        }

        composeTestRule.onNodeWithText("Add More").assertDoesNotExist()
    }

    @Test
    fun addExpenseScreen_addMoreButton_isDisabledWhenFormInvalid() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        // Amount is empty — Add More must be disabled
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun addExpenseScreen_addMoreButton_isEnabledWhenFormValid() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("200")

        composeTestRule.onNode(hasText("Add More") and hasClickAction()).assertIsEnabled()
    }

    @Test
    fun addExpenseScreen_addMoreButton_callsViewModelAndClearsAmount_forExpense() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("300")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        verify { transactionVM.addTransaction(any(), any()) }
        // After Add More, amount is cleared — Add button must be disabled
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsNotEnabled()
    }

    @Test
    fun addExpenseScreen_addMoreButton_callsViewModelForIncome() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Income") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("1500")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        verify { transactionVM.addTransaction(any(), any()) }
        // Form should reset — Add button disabled again
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsNotEnabled()
    }

    @Test
    fun addExpenseScreen_addMoreButton_callsViewModelForTransfer() {
        accountsFlow.value = listOf("Bank", "Cash")
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Transfer").performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("2000")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        verify {
            transactionVM.addAccountTransfer(
                fromAccount = any(),
                toAccount = any(),
                amount = 2000.0,
                paymentMethod = any(),
                description = any(),
                transactionDate = any()
            )
        }
        // Form should reset — Add button disabled again
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertIsNotEnabled()
    }

    @Test
    fun addExpenseScreen_addMoreButton_canAddMultipleTransactions() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Amount").performTextInput("200")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        verify(exactly = 2) { transactionVM.addTransaction(any(), any()) }
    }

    @Test
    fun addExpenseScreen_addButton_showsAddLabel() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertExists()
    }

    @Test
    fun addExpenseScreen_bothButtons_areDisplayedSideBySide() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).assertExists()
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).assertExists()
    }

    @Test
    fun addExpenseScreen_addButton_callsDismiss() {
        var dismissCalled = false
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = { dismissCalled = true })
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onNode(hasText("Add") and hasClickAction() and !hasText("Add More")).performClick()

        assertTrue(dismissCalled)
    }

    @Test
    fun addExpenseScreen_addMoreButton_doesNotCallDismiss() {
        var dismissCalled = false
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = { dismissCalled = true })
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("100")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        assertFalse(dismissCalled)
    }

    @Test
    fun addExpenseScreen_addMoreButton_resetsTabToExpense_afterIncome() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNode(hasText("Income") and hasClickAction()).performClick()
        composeTestRule.onNodeWithText("Amount").performTextInput("500")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        // Income tab must no longer be selected after reset to EXPENSE
        composeTestRule.onNode(hasText("Income") and isSelected()).assertDoesNotExist()
    }

    @Test
    fun addExpenseScreen_addMoreButton_clearsDescription() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Amount").performTextInput("300")
        composeTestRule.onNodeWithText("Description").performTextInput("SomeDescription")
        composeTestRule.onNode(hasText("Add More") and hasClickAction()).performClick()

        composeTestRule.onNodeWithText("SomeDescription").assertDoesNotExist()
    }

    // ── Feature 5: Payment Method Dropdown ──────────────────────────────────

    @Test
    fun paymentMethod_defaultCashIsShown() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cash").assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_doesNotCallLoadPaymentMethods_onLaunch() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        verify(exactly = 0) { paymentMethodVM.loadPaymentMethods() }
    }

    @Test
    fun addExpenseScreen_doesNotCallLoadCategories_onLaunch() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        verify(exactly = 0) { categoryVM.loadCategories() }
    }

    @Test
    fun addExpenseScreen_doesNotCallLoadAccounts_onLaunch() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        verify(exactly = 0) { accountVM.loadAccounts() }
    }

    @Test
    fun paymentMethod_dropdown_showsOptions() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cash").performClick()

        composeTestRule.onNodeWithText("UPI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Card").assertIsDisplayed()
    }

    @Test
    fun paymentMethod_dropdown_selectingOptionUpdatesForm() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cash").performClick()
        composeTestRule.onNodeWithText("UPI").performClick()

        composeTestRule.onNodeWithText("UPI").assertIsDisplayed()
    }

    // ── Feature 1: Date label ────────────────────────────────────────────────

    @Test
    fun addExpenseScreen_dateLabel_doesNotShowDatePrefix() {
        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Date:").assertDoesNotExist()
        composeTestRule.onNodeWithText("Date").assertDoesNotExist()
    }

    @Test
    fun addExpenseScreen_dateButton_clickOpensDatePicker() {
        val fixedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        selectedDateRangeFlow.value = DateRange.Day(fixedDate)

        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        val dateLabel = SimpleDateFormat(AppConstants.DATE_FORMAT_UI, Locale.getDefault()).format(fixedDate)
        composeTestRule.onNodeWithText(dateLabel).performClick()

        // The date picker dialog's OK button must now be visible
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun addExpenseScreen_dateButton_dismissingPickerRestoresOriginalLabel() {
        val fixedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        selectedDateRangeFlow.value = DateRange.Day(fixedDate)
        val dateLabel = SimpleDateFormat(AppConstants.DATE_FORMAT_UI, Locale.getDefault()).format(fixedDate)

        composeTestRule.setContent {
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = {})
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(dateLabel).performClick()
        // Two "Cancel" nodes exist: TopAppBar nav icon + date picker dialog button.
        // Select the one that has "OK" as a sibling — that's the dialog's Cancel.
        composeTestRule.onNode(hasText("Cancel") and hasAnySibling(hasText("OK"))).performClick()

        // Picker closed and original date label is still shown
        composeTestRule.onNodeWithText(dateLabel).assertIsDisplayed()
    }
}
