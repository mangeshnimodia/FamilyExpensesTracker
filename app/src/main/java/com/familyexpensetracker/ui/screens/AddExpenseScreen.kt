package com.familyexpensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.model.TransactionType
import com.familyexpensetracker.ui.components.AppDatePicker
import com.familyexpensetracker.ui.components.AppDropdown
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import com.familyexpensetracker.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit,
    transactionToEdit: Transaction? = null,
    isCopy: Boolean = false,
) {
    val amountFocusRequester = remember { FocusRequester() }
    var transactionType by remember {
        mutableStateOf(
            transactionToEdit?.let { if (it.amount < 0) TransactionType.EXPENSE else TransactionType.INCOME }
                ?: TransactionType.EXPENSE,
        )
    }
    var amount by remember {
        mutableStateOf(
            value = transactionToEdit?.let { kotlin.math.abs(it.amount).toInt().toString() } ?: ""
        )
    }
    var category by remember {
        mutableStateOf(
            value = transactionToEdit?.category ?: AppConstants.DEFAULT_CATEGORY
        )
    }
    var subcategory by remember {
        mutableStateOf(
            value = transactionToEdit?.subcategory ?: AppConstants.DEFAULT_SUBCATEGORY
        )
    }
    var paymentMethod by remember {
        mutableStateOf(
            value = transactionToEdit?.paymentMethod ?: AppConstants.DEFAULT_PAYMENT_METHOD
        )
    }
    var account by remember {
        mutableStateOf(
            value = transactionToEdit?.account ?: AppConstants.DEFAULT_ACCOUNT
        )
    }
    var toAccount by remember {
        mutableStateOf(
            value = AppConstants.DEFAULT_ACCOUNT
        )
    }
    var description by remember {
        mutableStateOf(
            value = transactionToEdit?.description ?: ""
        )
    }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val categoriesMap = if (transactionType == TransactionType.EXPENSE) expenseCategories else incomeCategories

    val accountsList by viewModel.accounts.collectAsState()
    var categoryExpanded by remember { mutableStateOf(value = false) }
    var subcategoryExpanded by remember { mutableStateOf(value = false) }
    var accountExpanded by remember { mutableStateOf(value = false) }
    var toAccountExpanded by remember { mutableStateOf(value = false) }

    val dateFormatter = remember { SimpleDateFormat(AppConstants.DATE_FORMAT_DB, Locale.getDefault()) }

    LaunchedEffect(transactionType) {
        if (transactionToEdit == null) {
            if (transactionType == TransactionType.EXPENSE) {
                category = AppConstants.DEFAULT_CATEGORY
                subcategory = AppConstants.DEFAULT_SUBCATEGORY
            } else if (transactionType == TransactionType.INCOME) {
                category = AppConstants.DEFAULT_INCOME_CATEGORY
                subcategory = AppConstants.DEFAULT_INCOME_SUBCATEGORY
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadAccounts()
        try {
            amountFocusRequester.requestFocus()
        } catch (_: Exception) {
            // Ignore if focus requester is not ready yet during initial composition in tests
        }
    }
    
    val currentRange by viewModel.selectedDateRange.collectAsState()
    val initialDate = remember(currentRange, transactionToEdit) {
        transactionToEdit?.let {
            try {
                dateFormatter.parse(it.date)
            } catch (_: Exception) {
                null
            }
        } ?: (currentRange as? DateRange.Day)?.date ?: Calendar.getInstance().time
    }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(value = false) }

    if (showDatePicker) {
        AppDatePicker(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
        ) {
            showDatePicker = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCopy) "Copy Entry" else if (transactionToEdit == null) "Add Entry" else "Edit Entry") },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Transaction Type Toggle
            val tabs = TransactionType.entries
            TabRow(
                selectedTabIndex = tabs.indexOf(transactionType),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
            ) {
                tabs.forEach { type ->
                    Tab(
                        selected = transactionType == type,
                        onClick = { transactionType = type },
                        text = {
                            Text(
                                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Date: ${dateFormatter.format(selectedDate)}")
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester),
            )

            if (transactionType != TransactionType.TRANSFER) {
                AppDropdown(
                    label = "Category",
                    selectedValue = category,
                    options = categoriesMap.keys.toList(),
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    onValueSelected = {
                        category = it
                        subcategory = ""
                    },
                    onDismiss = { categoryExpanded = false },
                    defaultValue = if (transactionType == TransactionType.EXPENSE) AppConstants.DEFAULT_CATEGORY else AppConstants.DEFAULT_INCOME_CATEGORY,
                )

                AppDropdown(
                    label = "Subcategory",
                    selectedValue = subcategory,
                    options = categoriesMap[category] ?: emptyList(),
                    expanded = subcategoryExpanded,
                    enabled = category.isNotBlank(),
                    onExpandedChange = { subcategoryExpanded = it },
                    onValueSelected = { subcategory = it },
                    onDismiss = { subcategoryExpanded = false },
                    defaultValue = if (transactionType == TransactionType.EXPENSE) AppConstants.DEFAULT_SUBCATEGORY else AppConstants.DEFAULT_INCOME_SUBCATEGORY,
                )
            }

            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { Text("Payment Method") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (transactionType == TransactionType.TRANSFER) {
                AppDropdown(
                    label = "From Account",
                    selectedValue = account,
                    options = accountsList,
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it },
                    onValueSelected = { account = it },
                    onDismiss = { accountExpanded = false },
                    defaultValue = AppConstants.DEFAULT_ACCOUNT,
                )

                AppDropdown(
                    label = "To Account",
                    selectedValue = toAccount,
                    options = accountsList,
                    expanded = toAccountExpanded,
                    onExpandedChange = { toAccountExpanded = it },
                    onValueSelected = { toAccount = it },
                    onDismiss = { toAccountExpanded = false },
                    defaultValue = AppConstants.DEFAULT_ACCOUNT,
                )
            } else {
                AppDropdown(
                    label = "Account",
                    selectedValue = account,
                    options = accountsList,
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it },
                    onValueSelected = { account = it },
                    onDismiss = { accountExpanded = false },
                    defaultValue = AppConstants.DEFAULT_ACCOUNT,
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    if (transactionType == TransactionType.TRANSFER) {
                        viewModel.addAccountTransfer(
                            fromAccount = account,
                            toAccount = toAccount,
                            amount = amountDouble,
                            paymentMethod = paymentMethod,
                            description = description,
                            transactionDate = selectedDate
                        )
                    } else {
                        val finalAmount = if (transactionType == TransactionType.EXPENSE) {
                            -amountDouble
                        } else {
                            amountDouble
                        }
                        val transaction = Transaction(
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
                        if (transactionToEdit == null || isCopy) {
                            viewModel.addTransaction(transaction, selectedDate)
                        } else {
                            viewModel.updateTransaction(transaction, selectedDate)
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = (amount.toIntOrNull()?.let { it > 0 } == true) &&
                        (transactionType == TransactionType.TRANSFER || category.isNotBlank()) &&
                        paymentMethod.isNotBlank() &&
                        account.isNotBlank() &&
                        (transactionType != TransactionType.TRANSFER || toAccount.isNotBlank()),
            ) {
                val buttonText = when {
                    isCopy -> "Copy"
                    transactionToEdit == null -> when (transactionType) {
                        TransactionType.EXPENSE -> "Add Expense"
                        TransactionType.INCOME -> "Add Income"
                        TransactionType.TRANSFER -> "Add Transfer"
                    }
                    else -> "Update"
                }
                Text(buttonText)
            }
        }
    }
}
