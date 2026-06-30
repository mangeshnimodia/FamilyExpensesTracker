package com.familyexpensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

enum class YearlyViewMode { Date, Category }

@Composable
fun YearlyViewToggle(
    selected: YearlyViewMode,
    onSelected: (YearlyViewMode) -> Unit,
) {
    val modes = YearlyViewMode.entries
    TabRow(
        selectedTabIndex = modes.indexOf(selected),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
    ) {
        modes.forEach { mode ->
            Tab(
                selected = mode == selected,
                onClick = { onSelected(mode) },
                text = { Text(mode.name) },
            )
        }
    }
}
