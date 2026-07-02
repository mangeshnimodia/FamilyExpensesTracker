package com.familyexpensetracker.ui.screens

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionType
import com.familyexpensetracker.utils.AppConstants
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseFormStateTest {

    private val fmt = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault())

    private fun expense(txnId: String = "1", amount: Double = -100.0, category: String = "Food") =
        Transaction(txnId = txnId, date = "2026/06/01", amount = amount, category = category, subcategory = "")

    // ── screenTitle ──────────────────────────────────────────────────────────

    @Test
    fun `screenTitle is Add Entry for new transaction`() {
        assertEquals("Add Entry", AddExpenseFormState().screenTitle)
    }

    @Test
    fun `screenTitle is Edit Entry when editing`() {
        assertEquals("Edit Entry", AddExpenseFormState(transactionToEdit = expense()).screenTitle)
    }

    @Test
    fun `screenTitle is Copy Entry when isCopy`() {
        assertEquals("Copy Entry", AddExpenseFormState(transactionToEdit = expense(), isCopy = true).screenTitle)
    }

    // ── isFormValid ──────────────────────────────────────────────────────────

    @Test
    fun `isFormValid false when amount is empty`() {
        val s = AddExpenseFormState(); s.amount = ""
        assertFalse(s.isFormValid)
    }

    @Test
    fun `isFormValid false when amount is zero`() {
        val s = AddExpenseFormState(); s.amount = "0"
        assertFalse(s.isFormValid)
    }

    @Test
    fun `isFormValid true with all valid expense fields`() {
        val s = AddExpenseFormState()
        s.amount = "100"; s.category = "Food"; s.paymentMethod = "UPI"; s.account = "Savings"
        assertTrue(s.isFormValid)
    }

    @Test
    fun `isFormValid false when category blank for expense`() {
        val s = AddExpenseFormState()
        s.amount = "100"; s.category = ""; s.paymentMethod = "UPI"; s.account = "Savings"
        assertFalse(s.isFormValid)
    }

    @Test
    fun `isFormValid false when paymentMethod blank`() {
        val s = AddExpenseFormState()
        s.amount = "100"; s.category = "Food"; s.paymentMethod = ""; s.account = "Savings"
        assertFalse(s.isFormValid)
    }

    @Test
    fun `isFormValid true for transfer when category blank`() {
        val s = AddExpenseFormState()
        s.transactionType = TransactionType.TRANSFER
        s.amount = "100"; s.category = ""; s.paymentMethod = "UPI"
        s.account = "Savings"; s.toAccount = "Wallet"
        assertTrue(s.isFormValid)
    }

    @Test
    fun `isFormValid false for transfer when toAccount blank`() {
        val s = AddExpenseFormState()
        s.transactionType = TransactionType.TRANSFER
        s.amount = "100"; s.paymentMethod = "UPI"; s.account = "Savings"; s.toAccount = ""
        assertFalse(s.isFormValid)
    }

    // ── submitLabel ──────────────────────────────────────────────────────────

    @Test
    fun `submitLabel is Add Expense for new expense`() {
        val s = AddExpenseFormState(); s.transactionType = TransactionType.EXPENSE
        assertEquals("Add Expense", s.submitLabel)
    }

    @Test
    fun `submitLabel is Add Income for new income`() {
        val s = AddExpenseFormState(); s.transactionType = TransactionType.INCOME
        assertEquals("Add Income", s.submitLabel)
    }

    @Test
    fun `submitLabel is Add Transfer for new transfer`() {
        val s = AddExpenseFormState(); s.transactionType = TransactionType.TRANSFER
        assertEquals("Add Transfer", s.submitLabel)
    }

    @Test
    fun `submitLabel is Update when editing`() {
        assertEquals("Update", AddExpenseFormState(transactionToEdit = expense()).submitLabel)
    }

    @Test
    fun `submitLabel is Copy when isCopy`() {
        assertEquals("Copy", AddExpenseFormState(transactionToEdit = expense(), isCopy = true).submitLabel)
    }

    // ── buildTransaction ─────────────────────────────────────────────────────

    @Test
    fun `buildTransaction expense has negative amount`() {
        val s = AddExpenseFormState(dateFormatter = fmt)
        s.amount = "500"; s.category = "Food"; s.paymentMethod = "UPI"; s.account = "Savings"
        s.selectedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        val t = s.buildTransaction()
        assertEquals(-500.0, t.amount, 0.0)
        assertEquals("2026/06/15", t.date)
    }

    @Test
    fun `buildTransaction income has positive amount`() {
        val s = AddExpenseFormState(dateFormatter = fmt)
        s.transactionType = TransactionType.INCOME
        s.amount = "2000"
        s.selectedDate = Calendar.getInstance().apply { set(2026, Calendar.JUNE, 15) }.time
        assertEquals(2000.0, s.buildTransaction().amount, 0.0)
    }

    @Test
    fun `buildTransaction preserves txnId when editing`() {
        val s = AddExpenseFormState(transactionToEdit = expense("orig-id"), dateFormatter = fmt)
        s.amount = "100"
        assertEquals("orig-id", s.buildTransaction().txnId)
    }

    @Test
    fun `buildTransaction uses new UUID and null transferId when isCopy`() {
        val s = AddExpenseFormState(transactionToEdit = expense("orig-id"), isCopy = true, dateFormatter = fmt)
        s.amount = "100"
        val t = s.buildTransaction()
        assertNotEquals("orig-id", t.txnId)
        assertNull(t.transferId)
    }

    // ── buildTransferParams ──────────────────────────────────────────────────

    @Test
    fun `buildTransferParams extracts all transfer fields`() {
        val s = AddExpenseFormState(dateFormatter = fmt)
        s.transactionType = TransactionType.TRANSFER
        s.amount = "300"; s.account = "Savings"; s.toAccount = "Wallet"
        s.paymentMethod = "NEFT"; s.description = "move funds"
        val date = Calendar.getInstance().time
        s.selectedDate = date
        val p = s.buildTransferParams()
        assertEquals("Savings", p.fromAccount)
        assertEquals("Wallet", p.toAccount)
        assertEquals(300.0, p.amount, 0.0)
        assertEquals("NEFT", p.paymentMethod)
        assertEquals("move funds", p.description)
        assertEquals(date, p.transactionDate)
    }

    // ── resetCategoryForType ──────────────────────────────────────────────────

    @Test
    fun `resetCategoryForType sets expense defaults`() {
        val s = AddExpenseFormState()
        s.transactionType = TransactionType.EXPENSE
        s.category = "Custom"
        s.resetCategoryForType()
        assertEquals(AppConstants.DEFAULT_CATEGORY, s.category)
        assertEquals(AppConstants.DEFAULT_SUBCATEGORY, s.subcategory)
    }

    @Test
    fun `resetCategoryForType sets income defaults`() {
        val s = AddExpenseFormState()
        s.transactionType = TransactionType.INCOME
        s.resetCategoryForType()
        assertEquals(AppConstants.DEFAULT_INCOME_CATEGORY, s.category)
        assertEquals(AppConstants.DEFAULT_INCOME_SUBCATEGORY, s.subcategory)
    }

    @Test
    fun `resetCategoryForType does nothing when editing existing transaction`() {
        val s = AddExpenseFormState(transactionToEdit = expense(category = "Custom"))
        s.resetCategoryForType()
        assertEquals("Custom", s.category)
    }
}
