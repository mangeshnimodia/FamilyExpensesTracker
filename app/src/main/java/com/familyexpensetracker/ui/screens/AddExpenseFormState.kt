package com.familyexpensetracker.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionType
import com.familyexpensetracker.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddExpenseFormState(
    val transactionToEdit: Transaction? = null,
    val isCopy: Boolean = false,
    val dateFormatter: SimpleDateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault()),
    val displayDateFormatter: SimpleDateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_UI, Locale.getDefault()),
) {
    var transactionType by mutableStateOf(
        transactionToEdit?.let { if (it.amount < 0) TransactionType.EXPENSE else TransactionType.INCOME }
            ?: TransactionType.EXPENSE
    )
    var amount by mutableStateOf(
        transactionToEdit?.let { kotlin.math.abs(it.amount).toInt().toString() } ?: ""
    )
    var category by mutableStateOf(transactionToEdit?.category ?: AppConstants.DEFAULT_CATEGORY)
    var subcategory by mutableStateOf(transactionToEdit?.subcategory ?: AppConstants.DEFAULT_SUBCATEGORY)
    var paymentMethod by mutableStateOf(transactionToEdit?.paymentMethod ?: AppConstants.DEFAULT_PAYMENT_METHOD)
    var account by mutableStateOf(transactionToEdit?.account ?: AppConstants.DEFAULT_ACCOUNT)
    var toAccount by mutableStateOf(AppConstants.DEFAULT_ACCOUNT)
    var description by mutableStateOf(transactionToEdit?.description ?: "")
    var selectedDate by mutableStateOf<Date>(Calendar.getInstance().time)
    var showDatePicker by mutableStateOf(false)
    var categoryExpanded by mutableStateOf(false)
    var subcategoryExpanded by mutableStateOf(false)
    var accountExpanded by mutableStateOf(false)
    var toAccountExpanded by mutableStateOf(false)

    val screenTitle: String
        get() = if (isCopy) "Copy Entry" else if (transactionToEdit == null) "Add Entry" else "Edit Entry"

    val isFormValid: Boolean
        get() = (amount.toIntOrNull()?.let { it > 0 } == true) &&
                (transactionType == TransactionType.TRANSFER || category.isNotBlank()) &&
                paymentMethod.isNotBlank() &&
                account.isNotBlank() &&
                (transactionType != TransactionType.TRANSFER || toAccount.isNotBlank())

    val submitLabel: String
        get() = when {
            isCopy -> "Copy"
            transactionToEdit == null -> when (transactionType) {
                TransactionType.EXPENSE  -> "Add Expense"
                TransactionType.INCOME   -> "Add Income"
                TransactionType.TRANSFER -> "Add Transfer"
            }
            else -> "Update"
        }

    fun resetCategoryForType() {
        if (transactionToEdit != null) return
        when (transactionType) {
            TransactionType.EXPENSE -> {
                category = AppConstants.DEFAULT_CATEGORY
                subcategory = AppConstants.DEFAULT_SUBCATEGORY
            }
            TransactionType.INCOME -> {
                category = AppConstants.DEFAULT_INCOME_CATEGORY
                subcategory = AppConstants.DEFAULT_INCOME_SUBCATEGORY
            }
            TransactionType.TRANSFER -> Unit
        }
    }

    fun buildTransaction(): Transaction {
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        val finalAmount = if (transactionType == TransactionType.EXPENSE) -amountDouble else amountDouble
        return Transaction(
            txnId = if (isCopy) UUID.randomUUID().toString() else transactionToEdit?.txnId ?: UUID.randomUUID().toString(),
            date = dateFormatter.format(selectedDate),
            amount = finalAmount,
            category = category,
            subcategory = subcategory,
            paymentMethod = paymentMethod,
            description = description,
            account = account,
            transferId = if (isCopy) null else transactionToEdit?.transferId,
        )
    }

    fun buildTransferParams(): TransferParams = TransferParams(
        fromAccount = account,
        toAccount = toAccount,
        amount = amount.toDoubleOrNull() ?: 0.0,
        paymentMethod = paymentMethod,
        description = description,
        transactionDate = selectedDate,
    )
}

data class TransferParams(
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val paymentMethod: String,
    val description: String,
    val transactionDate: Date,
)
