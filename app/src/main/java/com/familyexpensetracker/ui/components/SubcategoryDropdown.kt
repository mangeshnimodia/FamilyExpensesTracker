package com.familyexpensetracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryDropdown(
    selectedSubcategory: String,
    subcategories: List<String>,
    expanded: Boolean,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSubcategorySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) onExpandedChange(it) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedSubcategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Subcategory") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            enabled = enabled,
        )
        if (subcategories.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
            ) {
                subcategories.forEach { sub ->
                    DropdownMenuItem(
                        text = { Text(sub) },
                        onClick = {
                            onSubcategorySelected(sub)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}
