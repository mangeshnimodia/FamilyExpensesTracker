package com.familyexpensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.familyexpensetracker.ui.screens.PeriodTab

@Composable
fun PeriodTabBar(
    selectedTab: PeriodTab,
    onTabSelected: (PeriodTab) -> Unit,
) {
    val tabs = PeriodTab.entries
    TabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.name) }
            )
        }
    }
}
