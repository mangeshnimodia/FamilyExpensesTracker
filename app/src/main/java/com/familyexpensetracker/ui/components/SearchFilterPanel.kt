package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.SearchFilter

@Composable
fun SearchFilterPanel(
    filter: SearchFilter,
    accounts: List<String>,
    categories: List<String>,
    subcategories: List<String>,
    paymentMethods: List<String>,
    onFilterChange: (SearchFilter) -> Unit,
    onSearch: () -> Unit = {},
    onClear: () -> Unit = {},
) {
    var accountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppDropdown(
            label = "Account",
            selectedValue = filter.account,
            options = listOf("") + accounts,
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = it },
            onValueSelected = { onFilterChange(filter.copy(account = it)) },
            onDismiss = { accountExpanded = false },
        )

        AppDropdown(
            label = "Category",
            selectedValue = filter.category,
            options = listOf("") + categories,
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            onValueSelected = { onFilterChange(filter.copy(category = it)) },
            onDismiss = { categoryExpanded = false },
        )

        AppDropdown(
            label = "Subcategory",
            selectedValue = filter.subcategory,
            options = listOf("") + subcategories,
            expanded = subcategoryExpanded,
            onExpandedChange = { subcategoryExpanded = it },
            onValueSelected = { onFilterChange(filter.copy(subcategory = it)) },
            onDismiss = { subcategoryExpanded = false },
        )

        AppDropdown(
            label = "Payment Method",
            selectedValue = filter.paymentMethod,
            options = listOf("") + paymentMethods,
            expanded = paymentMethodExpanded,
            onExpandedChange = { paymentMethodExpanded = it },
            onValueSelected = { onFilterChange(filter.copy(paymentMethod = it)) },
            onDismiss = { paymentMethodExpanded = false },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = filter.minAmount?.toString() ?: "",
                onValueChange = { text ->
                    onFilterChange(filter.copy(minAmount = text.toDoubleOrNull()))
                },
                label = { Text("Min Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = filter.maxAmount?.toString() ?: "",
                onValueChange = { text ->
                    onFilterChange(filter.copy(maxAmount = text.toDoubleOrNull()))
                },
                label = { Text("Max Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = filter.exactMatch,
                onCheckedChange = { onFilterChange(filter.copy(exactMatch = it)) },
            )
            Text(
                text = "Exact match",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
            Button(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("Search") }
        }
    }
}
