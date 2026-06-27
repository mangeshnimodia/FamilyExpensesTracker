package com.familyexpensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var subcategory by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var account by remember { mutableStateOf("Passbook") }
    var description by remember { mutableStateOf("") }
    var transferId by remember { mutableStateOf("") }
    
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${dateFormatter.format(selectedDate)}")
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount (Positive Integer)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subcategory,
                onValueChange = { subcategory = it },
                label = { Text("Subcategory") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = paymentMethod,
                onValueChange = { paymentMethod = it },
                label = { Text("Payment Method") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Account") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = transferId,
                onValueChange = { transferId = it },
                label = { Text("Transfer ID (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountInt = amount.toIntOrNull() ?: 0
                    val transaction = Transaction(
                        txnId = UUID.randomUUID().toString(),
                        date = dateFormatter.format(selectedDate),
                        amount = amountInt.toDouble(),
                        category = category,
                        subcategory = subcategory,
                        paymentMethod = paymentMethod,
                        description = description,
                        account = account,
                        transferId = if (transferId.isBlank()) null else transferId
                    )
                    viewModel.addTransaction(transaction, selectedDate)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toIntOrNull()?.let { it > 0 } == true &&
                        category.isNotBlank() &&
                        paymentMethod.isNotBlank() &&
                        account.isNotBlank()
            ) {
                Text("Add Expense")
            }
        }
    }
}
