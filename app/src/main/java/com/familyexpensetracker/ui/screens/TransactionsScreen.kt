package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.familyexpensetracker.ui.theme.AppColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.ui.components.*
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
    val selectedPeriodTab by viewModel.selectedPeriodTab.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val categorySummaries by viewModel.categorySummaries.collectAsState()
    val monthSummaries by viewModel.monthSummaries.collectAsState()
    val expenseTotal by viewModel.expenseTotal.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val accountBalance by viewModel.accountBalance.collectAsState()

    val totalAmount = transactions.sumOf { it.amount }
    val snackbarHostState = remember { SnackbarHostState() }
    val dateRangeFormatter = remember { DateRangeFormatter() }
    val periodNavigator = remember { PeriodNavigator() }
    var yearlyViewMode by remember { mutableStateOf(YearlyViewMode.Category) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
    }

    LaunchedEffect(selectedPeriodTab, selectedFilter) {
        expandedCategory = null
    }


    NavHost(navController = navController, startDestination = NavRoutes.MAIN_LIST) {
        composable(NavRoutes.MAIN_LIST) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { AccountBalanceText(balance = accountBalance) },
                        navigationIcon = {
                            AccountDropdown(
                                accounts = availableAccounts,
                                selectedAccount = selectedAccount,
                                onAccountSelected = { account ->
                                    viewModel.setSelectedAccount(account)
                                    viewModel.setSelectedFilter(
                                        selectedFilter.copy(
                                            selectedAccounts = if (account != null) listOf(account) else emptyList()
                                        )
                                    )
                                    viewModel.fetchAccountBalance()
                                }
                            )
                        },
                        actions = {
                            IconButton(onClick = { (context as? Activity)?.finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
                            }
                        },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(NavRoutes.ADD) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search description") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        trailingIcon = {
                            if (searchText.isNotBlank()) {
                                IconButton(onClick = {
                                    searchText = ""
                                    viewModel.searchTransactions("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                    )

                    PeriodTabBar(
                        selectedTab = selectedPeriodTab,
                        onTabSelected = { tab ->
                            viewModel.setSelectedPeriodTab(tab)
                            viewModel.setSelectedDateRange(periodNavigator.defaultRangeFor(tab))
                        }
                    )

                    val periodTotal = when (selectedFilter.type) {
                        FilterType.EXPENSE -> expenseTotal
                        FilterType.INCOME -> transactions.filter { it.amount > 0 }.sumOf { it.amount }
                        FilterType.BALANCE -> totalAmount
                    }
                    val periodColor = when (selectedFilter.type) {
                        FilterType.INCOME -> AppColors.incomeGreen
                        FilterType.EXPENSE -> MaterialTheme.colorScheme.error
                        else -> if (periodTotal < 0) MaterialTheme.colorScheme.error else AppColors.incomeGreen
                    }
                    if (selectedPeriodTab != PeriodTab.All) {
                        PeriodNavBar(
                            dateRange = selectedDateRange,
                            label = dateRangeFormatter.format(selectedDateRange),
                            onPrevious = { viewModel.setSelectedDateRange(periodNavigator.previous(selectedDateRange)) },
                            onNext = { viewModel.setSelectedDateRange(periodNavigator.next(selectedDateRange)) },
                            navigator = periodNavigator,
                            expenseTotal = periodTotal,
                            totalColor = periodColor,
                        )
                    } else {
                        TotalBanner(
                            total = periodTotal,
                            totalColor = periodColor,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "Expense" to FilterType.EXPENSE,
                            "Balance" to FilterType.BALANCE,
                            "Income" to FilterType.INCOME,
                        ).forEach { (label, type) ->
                            FilterChip(
                                selected = selectedFilter.type == type,
                                onClick = { viewModel.setSelectedFilter(selectedFilter.copy(type = type)) },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (selectedPeriodTab == PeriodTab.Yearly) {
                        YearlyViewToggle(selected = yearlyViewMode, onSelected = { yearlyViewMode = it })
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (searchQuery.isNotBlank()) {
                            when {
                                isSearching -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                searchResults.isEmpty() -> Text(
                                    "No results for \"$searchQuery\"",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                else -> TransactionList(
                                    transactions = searchResults,
                                    onEdit = { txn -> navController.navigate(NavRoutes.edit(txn.txnId)) },
                                    onCopy = { txn -> navController.navigate(NavRoutes.copy(txn.txnId)) },
                                    onDelete = { txn -> viewModel.deleteTransaction(txn.txnId) },
                                )
                            }
                        } else {
                            when {
                                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                transactions.isEmpty() -> Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text("No transactions loaded", style = MaterialTheme.typography.bodyLarge)
                                    Text("Tap refresh to fetch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                                selectedPeriodTab == PeriodTab.Yearly && yearlyViewMode == YearlyViewMode.Date ->
                                    MonthlyBreakdownList(
                                        summaries = monthSummaries,
                                        onMonthClick = { monthRange ->
                                            viewModel.setSelectedPeriodTab(PeriodTab.Monthly)
                                            viewModel.setSelectedDateRange(monthRange)
                                        }
                                    )
                                else -> {
                                    val selectedCategoryName = expandedCategory
                                    if (selectedCategoryName == null) {
                                        CategorySummaryList(
                                            summaries = categorySummaries,
                                            onCategoryClick = { summary ->
                                                expandedCategory = summary.category
                                            }
                                        )
                                    } else {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                IconButton(onClick = { expandedCategory = null }) {
                                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                                }
                                                Text(
                                                    text = selectedCategoryName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                )
                                            }
                                            HorizontalDivider()
                                            TransactionList(
                                                transactions = transactions.filter { it.category == selectedCategoryName },
                                                onEdit = { txn -> navController.navigate(NavRoutes.edit(txn.txnId)) },
                                                onCopy = { txn -> navController.navigate(NavRoutes.copy(txn.txnId)) },
                                                onDelete = { txn -> viewModel.deleteTransaction(txn.txnId) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                if (searchText.isNotBlank()) {
                                    viewModel.searchTransactions(searchText)
                                } else {
                                    viewModel.fetchTransactions()
                                }
                                viewModel.fetchAccountBalance()
                            },
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }
        }
        composable(NavRoutes.ADD) {
            AddExpenseScreen(viewModel = viewModel, onDismiss = { navController.popBackStack() })
        }
        composable(NavRoutes.EDIT) { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
                ?: searchResults.find { it.txnId == txnId }
            AddExpenseScreen(viewModel = viewModel, onDismiss = { navController.popBackStack() }, transactionToEdit = transaction)
        }
        composable(NavRoutes.COPY) { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
                ?: searchResults.find { it.txnId == txnId }
            AddExpenseScreen(viewModel = viewModel, onDismiss = { navController.popBackStack() }, transactionToEdit = transaction, isCopy = true)
        }
        composable(NavRoutes.SUBCATEGORY) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            SubcategoryDetailScreen(viewModel = viewModel, category = category, navController = navController)
        }
    }
}
