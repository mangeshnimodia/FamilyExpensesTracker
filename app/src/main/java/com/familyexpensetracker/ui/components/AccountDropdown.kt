package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountDropdown(
    accounts: List<String>,
    selectedAccount: String?,
    onAccountSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val chipLabel = selectedAccount ?: "All Accounts"

    Box(modifier = Modifier.padding(end = 8.dp)) {
        FilterChip(
            selected = selectedAccount != null,
            onClick = { expanded = true },
            label = { Text(chipLabel) },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All Accounts") },
                onClick = { onAccountSelected(null); expanded = false },
            )
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account) },
                    onClick = { onAccountSelected(account); expanded = false },
                )
            }
        }
    }
}
