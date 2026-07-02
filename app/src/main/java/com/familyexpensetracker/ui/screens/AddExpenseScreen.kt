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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit,
    transactionToEdit: Transaction? = null,
    isCopy: Boolean = false,
) {
    val amountFocusRequester = remember { FocusRequester() }
    val formState = remember(transactionToEdit, isCopy) { AddExpenseFormState(transactionToEdit, isCopy) }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val categoriesMap = if (formState.transactionType == TransactionType.EXPENSE) expenseCategories else incomeCategories
    val accountsList by viewModel.accounts.collectAsState()

    val currentRange by viewModel.selectedDateRange.collectAsState()
    val initialDate = remember(currentRange, transactionToEdit) {
        transactionToEdit?.let {
            try { formState.dateFormatter.parse(it.date) } catch (_: Exception) { null }
        } ?: (currentRange as? DateRange.Day)?.date ?: Calendar.getInstance().time
    }

    LaunchedEffect(initialDate) { formState.selectedDate = initialDate }

    LaunchedEffect(formState.transactionType) { formState.resetCategoryForType() }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        viewModel.loadAccounts()
        try { amountFocusRequester.requestFocus() } catch (_: Exception) { }
    }

    if (formState.showDatePicker) {
        AppDatePicker(
            initialDate = formState.selectedDate,
            onDateSelected = { formState.selectedDate = it },
        ) { formState.showDatePicker = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(formState.screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
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
            val tabs = TransactionType.entries
            TabRow(
                selectedTabIndex = tabs.indexOf(formState.transactionType),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
            ) {
                tabs.forEach { type ->
                    Tab(
                        selected = formState.transactionType == type,
                        onClick = { formState.transactionType = type },
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { formState.showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Date: ${'$'}{formState.dateFormatter.format(formState.selectedDate)}")
            }

            OutlinedTextField(
                value = formState.amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) formState.amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester),
            )

            CategorySubcategoryFields(formState = formState, categoriesMap = categoriesMap)

            OutlinedTextField(
                value = formState.paymentMethod,
                onValueChange = { formState.paymentMethod = it },
                label = { Text("Payment Method") },
                modifier = Modifier.fillMaxWidth(),
            )

            AccountSelectionFields(formState = formState, accountsList = accountsList)

            OutlinedTextField(
                value = formState.description,
                onValueChange = { formState.description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (formState.transactionType == TransactionType.TRANSFER) {
                        val p = formState.buildTransferParams()
                        viewModel.addAccountTransfer(
                            fromAccount = p.fromAccount,
                            toAccount = p.toAccount,
                            amount = p.amount,
                            paymentMethod = p.paymentMethod,
                            description = p.description,
                            transactionDate = p.transactionDate,
                        )
                    } else {
                        val transaction = formState.buildTransaction()
                        if (transactionToEdit == null || isCopy) {
                            viewModel.addTransaction(transaction, formState.selectedDate)
                        } else {
                            viewModel.updateTransaction(transaction, formState.selectedDate)
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = formState.isFormValid,
            ) {
                Text(formState.submitLabel)
            }
        }
    }
}

@Composable
private fun CategorySubcategoryFields(
    formState: AddExpenseFormState,
    categoriesMap: Map<String, List<String>>,
) {
    if (formState.transactionType == TransactionType.TRANSFER) return
    AppDropdown(
        label = "Category",
        selectedValue = formState.category,
        options = categoriesMap.keys.toList(),
        expanded = formState.categoryExpanded,
        onExpandedChange = { formState.categoryExpanded = it },
        onValueSelected = { formState.category = it; formState.subcategory = "" },
        onDismiss = { formState.categoryExpanded = false },
        defaultValue = if (formState.transactionType == TransactionType.EXPENSE) AppConstants.DEFAULT_CATEGORY else AppConstants.DEFAULT_INCOME_CATEGORY,
    )
    AppDropdown(
        label = "Subcategory",
        selectedValue = formState.subcategory,
        options = categoriesMap[formState.category] ?: emptyList(),
        expanded = formState.subcategoryExpanded,
        enabled = formState.category.isNotBlank(),
        onExpandedChange = { formState.subcategoryExpanded = it },
        onValueSelected = { formState.subcategory = it },
        onDismiss = { formState.subcategoryExpanded = false },
        defaultValue = if (formState.transactionType == TransactionType.EXPENSE) AppConstants.DEFAULT_SUBCATEGORY else AppConstants.DEFAULT_INCOME_SUBCATEGORY,
    )
}

@Composable
private fun AccountSelectionFields(
    formState: AddExpenseFormState,
    accountsList: List<String>,
) {
    if (formState.transactionType == TransactionType.TRANSFER) {
        AppDropdown(
            label = "From Account",
            selectedValue = formState.account,
            options = accountsList,
            expanded = formState.accountExpanded,
            onExpandedChange = { formState.accountExpanded = it },
            onValueSelected = { formState.account = it },
            onDismiss = { formState.accountExpanded = false },
            defaultValue = AppConstants.DEFAULT_ACCOUNT,
        )
        AppDropdown(
            label = "To Account",
            selectedValue = formState.toAccount,
            options = accountsList,
            expanded = formState.toAccountExpanded,
            onExpandedChange = { formState.toAccountExpanded = it },
            onValueSelected = { formState.toAccount = it },
            onDismiss = { formState.toAccountExpanded = false },
            defaultValue = AppConstants.DEFAULT_ACCOUNT,
        )
    } else {
        AppDropdown(
            label = "Account",
            selectedValue = formState.account,
            options = accountsList,
            expanded = formState.accountExpanded,
            onExpandedChange = { formState.accountExpanded = it },
            onValueSelected = { formState.account = it },
            onDismiss = { formState.accountExpanded = false },
            defaultValue = AppConstants.DEFAULT_ACCOUNT,
        )
    }
}
