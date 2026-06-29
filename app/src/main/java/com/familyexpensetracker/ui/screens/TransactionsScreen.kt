package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familyexpensetracker.ui.components.DateRangeFormatter
import com.familyexpensetracker.ui.components.DateRangeSelector
import com.familyexpensetracker.ui.components.GroupedTransactionsView
import com.familyexpensetracker.ui.components.TransactionFilterDialog
import com.familyexpensetracker.ui.components.TransactionItem
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val availableAccounts by viewModel.accounts.collectAsState()
    
    val totalAmount = transactions.sumOf { it.amount }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val dateRangeFormatter = remember { DateRangeFormatter() }
    var showRangeSelector by remember { mutableStateOf(value = false) }
    var showFilterDialog by remember { mutableStateOf(value = false) }
    var viewMode by remember { mutableStateOf(TransactionViewMode.Activity) }
    val listState = rememberLazyListState()
    val scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showRangeSelector) {
        DateRangeSelector(
            onRangeSelected = { viewModel.setSelectedDateRange(it) },
        ) {
            showRangeSelector = false
        }
    }

    if (showFilterDialog) {
        TransactionFilterDialog(
            currentFilter = selectedFilter,
            availableAccounts = availableAccounts,
            onFilterApplied = { viewModel.setSelectedFilter(it) },
            onDismiss = { showFilterDialog = false }
        )
    }

    NavHost(navController = navController, startDestination = "main_list") {
        composable("main_list") {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = dateRangeFormatter.format(selectedDateRange),
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        actions = {
                            IconButton(onClick = {
                                viewModel.loadAccounts()
                                showFilterDialog = true
                            }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter Transactions")
                            }
                            IconButton(onClick = { showRangeSelector = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date Range")
                            }
                            IconButton(onClick = { (context as? Activity)?.finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit App")
                            }
                        },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate("add") }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Total Amount Highlight
                    if (!isLoading) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val totalColor = if (totalAmount < 0) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color(0xFF4CAF50) // Material Green
                                }
                                Text(
                                    text = "₹${"%.2f".format(kotlin.math.abs(totalAmount))}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = totalColor
                                )
                            }
                        }
                    }

                    // View Mode Toggle (Two "Bullets"/Tabs)
                    TabRow(
                        selectedTabIndex = if (viewMode == TransactionViewMode.Activity) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                    ) {
                        Tab(
                            selected = viewMode == TransactionViewMode.Activity,
                            onClick = { viewMode = TransactionViewMode.Activity },
                            text = { Text("Activity") }
                        )
                        Tab(
                            selected = viewMode == TransactionViewMode.GroupBy,
                            onClick = { viewMode = TransactionViewMode.GroupBy },
                            text = { Text("Grouped") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        } else if (transactions.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No transactions loaded",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "Tap 🔄 to fetch for this range",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            if (viewMode == TransactionViewMode.Activity) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawWithContent {
                                            drawContent()
                                            val firstVisibleElementIndex =
                                                listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                                            val needScrollbar =
                                                listState.layoutInfo.totalItemsCount > listState.layoutInfo.visibleItemsInfo.size

                                            if (needScrollbar && (firstVisibleElementIndex != null)) {
                                                val elementHeight =
                                                    size.height / listState.layoutInfo.totalItemsCount
                                                val scrollbarHeight =
                                                    listState.layoutInfo.visibleItemsInfo.size * elementHeight
                                                val scrollbarOffsetY =
                                                    firstVisibleElementIndex * elementHeight

                                                drawRect(
                                                    color = scrollbarColor,
                                                    topLeft = Offset(
                                                        size.width - 4.dp.toPx(),
                                                        scrollbarOffsetY
                                                    ),
                                                    size = Size(4.dp.toPx(), scrollbarHeight),
                                                )
                                            }
                                        },
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(transactions.size) { index ->
                                        val transaction = transactions[index]
                                        TransactionItem(
                                            txn = transaction,
                                            onDelete = {
                                                viewModel.deleteTransaction(transaction.txnId)
                                            },
                                            onEdit = {
                                                navController.navigate("edit/${it.txnId}")
                                            }
                                        )
                                    }
                                }
                            } else {
                                GroupedTransactionsView(
                                    transactions = transactions,
                                    onDelete = { viewModel.deleteTransaction(it) },
                                    onEdit = { navController.navigate("edit/${it.txnId}") }
                                )
                            }
                        }

                        // Refresh FAB in bottom left
                        FloatingActionButton(
                            onClick = { viewModel.fetchTransactions() },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }
        }
        composable("add") {
            AddExpenseScreen(
                viewModel = viewModel,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
        composable("edit/{txnId}") { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
            AddExpenseScreen(
                viewModel = viewModel,
                onDismiss = {
                    navController.popBackStack()
                },
                transactionToEdit = transaction
            )
        }
    }
}
