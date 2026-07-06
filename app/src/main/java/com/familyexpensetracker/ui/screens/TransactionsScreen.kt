package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.ui.components.*
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.PaymentMethodViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactionVM: TransactionViewModel,
    categoryVM: CategoryViewModel,
    accountVM: AccountViewModel,
    searchVM: SearchViewModel,
    paymentMethodVM: PaymentMethodViewModel,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val transactions by transactionVM.transactions.collectAsState()
    val isLoading by transactionVM.isLoading.collectAsState()
    val errorMessage by transactionVM.errorMessage.collectAsState()
    val selectedDateRange by transactionVM.selectedDateRange.collectAsState()
    val selectedFilter by transactionVM.selectedFilter.collectAsState()
    val availableAccounts by accountVM.accounts.collectAsState()
    val selectedPeriodTab by transactionVM.selectedPeriodTab.collectAsState()
    val selectedAccount by transactionVM.selectedAccount.collectAsState()
    val categorySummaries by transactionVM.categorySummaries.collectAsState()
    val monthSummaries by transactionVM.monthSummaries.collectAsState()
    val expenseTotal by transactionVM.expenseTotal.collectAsState()
    val searchQuery by searchVM.searchQuery.collectAsState()
    val searchResults by searchVM.searchResults.collectAsState()
    val isSearching by searchVM.isSearching.collectAsState()
    val searchFilter by searchVM.searchFilter.collectAsState()
    val isSearchActive by searchVM.isSearchActive.collectAsState()
    val accountBalance by transactionVM.accountBalance.collectAsState()
    val paymentMethods by paymentMethodVM.paymentMethods.collectAsState()
    val expenseCategories by categoryVM.expenseCategories.collectAsState()

    val totalAmount = transactions.sumOf { it.amount }
    val snackbarHostState = remember { SnackbarHostState() }
    val dateRangeFormatter = remember { DateRangeFormatter() }
    val periodNavigator = remember { PeriodNavigator() }
    var yearlyViewMode by remember { mutableStateOf(YearlyViewMode.Category) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }
    var showFilterPanel by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            transactionVM.clearError()
        }
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
                                    transactionVM.setSelectedAccount(account)
                                    transactionVM.setSelectedFilter(
                                        selectedFilter.copy(
                                            selectedAccounts = if (account != null) listOf(account) else emptyList()
                                        )
                                    )
                                    transactionVM.fetchAccountBalance()
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Search description") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                if (searchText.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        searchVM.searchTransactions("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                        )
                        IconButton(onClick = { searchVM.searchTransactions(searchText, searchFilter) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterPanel = !showFilterPanel }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Toggle filters")
                        }
                    }
                    if (showFilterPanel) {
                        val categoryNames = expenseCategories.keys.toList()
                        val subcategoryNames = if (searchFilter.category.isNotBlank()) {
                            expenseCategories[searchFilter.category] ?: emptyList()
                        } else {
                            expenseCategories.values.flatten().distinct()
                        }
                        SearchFilterPanel(
                            filter = searchFilter,
                            accounts = availableAccounts,
                            categories = categoryNames,
                            subcategories = subcategoryNames,
                            paymentMethods = paymentMethods,
                            onFilterChange = { searchVM.updateSearchFilter(it) },
                            onSearch = {
                                searchVM.searchTransactions(searchText, searchFilter)
                                showFilterPanel = false
                            },
                            onClear = {
                                searchText = ""
                                searchVM.clearSearch()
                                showFilterPanel = false
                                transactionVM.fetchTransactions()
                                transactionVM.fetchAccountBalance()
                            },
                        )
                    }

                    PeriodTabBar(
                        selectedTab = selectedPeriodTab,
                        onTabSelected = { tab ->
                            transactionVM.setSelectedPeriodTab(tab)
                            transactionVM.setSelectedDateRange(periodNavigator.defaultRangeFor(tab))
                        }
                    )

                    val periodTotal = when (selectedFilter.type) {
                        FilterType.EXPENSE -> expenseTotal
                        FilterType.INCOME -> transactions.filter { it.amount > 0 }.sumOf { it.amount }
                        FilterType.BALANCE -> totalAmount
                    }
                    if (selectedPeriodTab != PeriodTab.All) {
                        PeriodNavBar(
                            dateRange = selectedDateRange,
                            label = dateRangeFormatter.format(selectedDateRange),
                            onPrevious = { transactionVM.setSelectedDateRange(periodNavigator.previous(selectedDateRange)) },
                            onNext = { transactionVM.setSelectedDateRange(periodNavigator.next(selectedDateRange)) },
                            navigator = periodNavigator,
                            expenseTotal = periodTotal,
                            onDateSelected = { transactionVM.setSelectedDateRange(DateRange.Day(it)) },
                        )
                    } else {
                        TotalBanner(total = periodTotal)
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
                                onClick = { transactionVM.setSelectedFilter(selectedFilter.copy(type = type)) },
                                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (selectedPeriodTab == PeriodTab.Yearly) {
                        YearlyViewToggle(selected = yearlyViewMode, onSelected = { yearlyViewMode = it })
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isSearchActive) {
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
                                    onDelete = { txn -> transactionVM.deleteTransaction(txn.txnId) },
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
                                            transactionVM.setSelectedPeriodTab(PeriodTab.Monthly)
                                            transactionVM.setSelectedDateRange(monthRange)
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
                                                onDelete = { txn -> transactionVM.deleteTransaction(txn.txnId) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                searchVM.clearSearch()
                                searchText = ""
                                transactionVM.fetchTransactions()
                                transactionVM.fetchAccountBalance()
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
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = { navController.popBackStack() })
        }
        composable(NavRoutes.EDIT) { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
                ?: searchResults.find { it.txnId == txnId }
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = { navController.popBackStack() }, transactionToEdit = transaction)
        }
        composable(NavRoutes.COPY) { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
                ?: searchResults.find { it.txnId == txnId }
            AddExpenseScreen(transactionVM = transactionVM, categoryVM = categoryVM, accountVM = accountVM, paymentMethodVM = paymentMethodVM, onDismiss = { navController.popBackStack() }, transactionToEdit = transaction, isCopy = true)
        }
        composable(NavRoutes.SUBCATEGORY) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            SubcategoryDetailScreen(transactionVM = transactionVM, category = category, navController = navController)
        }
    }
}
