package com.familyexpensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.ui.theme.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familyexpensetracker.data.model.SubcategoryGroup
import com.familyexpensetracker.ui.components.TransactionItem
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryDetailScreen(
    viewModel: ExpenseViewModel,
    category: String,
    navController: NavController,
) {
    val categorySummaries by viewModel.categorySummaries.collectAsState()
    val subcategories = categorySummaries.find { it.category == category }?.subcategories ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(subcategories.size) { index ->
                SubcategoryGroupRow(
                    group = subcategories[index],
                    onDelete = { txnId -> viewModel.deleteTransaction(txnId) },
                    onEdit = { txn -> navController.navigate("edit/${txn.txnId}") },
                    onCopy = { txn -> navController.navigate("copy/${txn.txnId}") },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SubcategoryGroupRow(
    group: SubcategoryGroup,
    onDelete: (String) -> Unit,
    onEdit: (com.familyexpensetracker.data.model.Transaction) -> Unit,
    onCopy: (com.familyexpensetracker.data.model.Transaction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val amountColor = if (group.totalAmount < 0) AppColors.expenseRed else AppColors.summaryGreen

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.subcategory.ifBlank { "(none)" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                )
                Text(
                    text = "${group.transactionCount} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Text(
                text = "%.2f".format(abs(group.totalAmount)),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = amountColor,
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
        }
        if (expanded) {
            group.transactions.forEach { txn ->
                TransactionItem(
                    txn = txn,
                    onDelete = { onDelete(txn.txnId) },
                    onEdit = { onEdit(txn) },
                    onCopy = { onCopy(txn) },
                )
                HorizontalDivider()
            }
        }
    }
}
