package com.familyexpensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyexpensetracker.data.model.AccountGroup
import com.familyexpensetracker.data.model.CategoryGroup
import com.familyexpensetracker.data.model.Transaction
import kotlin.math.abs

@Composable
fun GroupedTransactionsView(
    transactions: List<Transaction>,
    onDelete: (String) -> Unit
) {
    if (transactions.isEmpty()) return

    val groupedData = remember(transactions) {
        transactions.groupBy { it.account }.map { (account, accountTxns) ->
            AccountGroup(
                name = account.ifBlank { "Unassigned" },
                total = accountTxns.sumOf { it.amount },
                categories = accountTxns.groupBy { it.category }.map { (category, categoryTxns) ->
                    CategoryGroup(
                        name = category.ifBlank { "Unassigned" },
                        total = categoryTxns.sumOf { it.amount },
                        transactions = categoryTxns
                    )
                }
            )
        }
    }

    val expandedAccounts = remember { mutableStateMapOf<String, Boolean>() }
    val expandedCategories = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedData.forEach { accountGroup ->
            item(key = "acc_${accountGroup.name}") {
                AccountHeader(
                    accountGroup = accountGroup,
                    isExpanded = expandedAccounts[accountGroup.name] ?: false,
                    onExpandClick = {
                        expandedAccounts[accountGroup.name] = !(expandedAccounts[accountGroup.name] ?: false)
                    }
                )
            }

            if (expandedAccounts[accountGroup.name] == true) {
                accountGroup.categories.forEach { categoryGroup ->
                    val categoryKey = "${accountGroup.name}_${categoryGroup.name}"
                    item(key = "cat_$categoryKey") {
                        CategoryHeader(
                            categoryGroup = categoryGroup,
                            isExpanded = expandedCategories[categoryKey] ?: false,
                            onExpandClick = {
                                expandedCategories[categoryKey] = !(expandedCategories[categoryKey] ?: false)
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    if (expandedCategories[categoryKey] == true) {
                        items(
                            categoryGroup.transactions,
                            key = { txn -> "${accountGroup.name}_${categoryGroup.name}_${txn.txnId}_${txn.hashCode()}" }
                        ) { txn ->
                            Box(modifier = Modifier.padding(start = 32.dp)) {
                                TransactionItem(
                                    txn = txn,
                                    onDelete = { onDelete(txn.txnId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountHeader(
    accountGroup: AccountGroup,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = accountGroup.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                val totalColor = if (accountGroup.total < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color(0xFF4CAF50)
                }
                Text(
                    text = "Subtotal: ₹${"%.2f".format(abs(accountGroup.total))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = totalColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}

@Composable
fun CategoryHeader(
    categoryGroup: CategoryGroup,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = categoryGroup.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val totalColor = if (categoryGroup.total < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color(0xFF4CAF50)
                }
                Text(
                    text = "Total: ₹${"%.2f".format(abs(categoryGroup.total))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = totalColor
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    }
}
