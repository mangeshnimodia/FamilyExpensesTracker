package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.ui.components.*
import com.familyexpensetracker.ui.theme.AppColors
import com.familyexpensetracker.ui.viewmodel.AccountViewModel
import com.familyexpensetracker.ui.viewmodel.CategoryViewModel
import com.familyexpensetracker.ui.viewmodel.SearchViewModel
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(
    navController: NavController,
    transactionVM: TransactionViewModel,
    categoryVM: CategoryViewModel,
    accountVM: AccountViewModel,
    searchVM: SearchViewModel,
) {
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
    val accountBalance by transactionVM.accountBalance.collectAsState()

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
            transactionVM.clearError()
        }
    }

    LaunchedEffect(Unit) {
        accountVM.loadAccounts()
    }

    LaunchedEffect(selectedPeriodTab, selectedFilter) {
        expandedCategory = null
    }

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
                            searchVM.searchTransactions("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
            )

            PeriodTabBar(
                selectedTab = selectedPeriodTab,
                onTabSelected = { tab ->
                    transactionVM.setSelectedPeriodTab(tab)
                    transactionVM.setSelectedDateRange(periodNavigator.defaultRangeFor(tab))
                }
            )

            PeriodDisplay(
                filterType = selectedFilter.type,
                expenseTotal = expenseTotal,
                totalAmount = totalAmount,
                transactions = transactions,
                selectedPeriodTab = selectedPeriodTab,
                selectedDateRange = selectedDateRange,
                navigator = periodNavigator,
                dateRangeFormatter = dateRangeFormatter,
                onPrevious = { transactionVM.setSelectedDateRange(periodNavigator.previous(selectedDateRange)) },
                onNext = { transactionVM.setSelectedDateRange(periodNavigator.next(selectedDateRange)) },
            )

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
                if (searchQuery.isNotBlank()) {
                    SearchResultsContent(
                        isSearching = isSearching,
                        searchResults = searchResults,
                        searchQuery = searchQuery,
                        navController = navController,
                        onDelete = { txn -> transactionVM.deleteTransaction(txn.txnId) },
                    )
                } else {
                    TransactionContent(
                        isLoading = isLoading,
                        transactions = transactions,
                        categorySummaries = categorySummaries,
                        monthSummaries = monthSummaries,
                        selectedPeriodTab = selectedPeriodTab,
                        yearlyViewMode = yearlyViewMode,
                        expandedCategory = expandedCategory,
                        onCategoryClick = { summary -> expandedCategory = summary.category },
                        onBack = { expandedCategory = null },
                        navController = navController,
                        onDelete = { txn -> transactionVM.deleteTransaction(txn.txnId) },
                        onMonthClick = { monthRange ->
                            transactionVM.setSelectedPeriodTab(PeriodTab.Monthly)
                            transactionVM.setSelectedDateRange(monthRange)
                        },
                    )
                }

                FloatingActionButton(
                    onClick = {
                        if (searchText.isNotBlank()) {
                            searchVM.searchTransactions(searchText)
                        } else {
                            transactionVM.fetchTransactions()
                        }
                        transactionVM.fetchAccountBalance()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}

@Composable
private fun PeriodDisplay(
    filterType: FilterType,
    expenseTotal: Double,
    totalAmount: Double,
    transactions: List<com.familyexpensetracker.data.model.Transaction>,
    selectedPeriodTab: PeriodTab,
    selectedDateRange: com.familyexpensetracker.data.model.DateRange,
    navigator: PeriodNavigator,
    dateRangeFormatter: DateRangeFormatter,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val periodTotal = when (filterType) {
        FilterType.EXPENSE -> expenseTotal
        FilterType.INCOME -> transactions.filter { it.amount > 0 }.sumOf { it.amount }
        FilterType.BALANCE -> totalAmount
    }
    val periodColor = when (filterType) {
        FilterType.INCOME -> AppColors.incomeGreen
        FilterType.EXPENSE -> MaterialTheme.colorScheme.error
        else -> if (periodTotal < 0) MaterialTheme.colorScheme.error else AppColors.incomeGreen
    }
    if (selectedPeriodTab != PeriodTab.All) {
        PeriodNavBar(
            dateRange = selectedDateRange,
            label = dateRangeFormatter.format(selectedDateRange),
            onPrevious = onPrevious,
            onNext = onNext,
            navigator = navigator,
            expenseTotal = periodTotal,
            totalColor = periodColor,
        )
    } else {
        TotalBanner(
            total = periodTotal,
            totalColor = periodColor,
        )
    }
}

@Composable
private fun SearchResultsContent(
    isSearching: Boolean,
    searchResults: List<com.familyexpensetracker.data.model.Transaction>,
    searchQuery: String,
    navController: NavController,
    onDelete: (com.familyexpensetracker.data.model.Transaction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun TransactionContent(
    isLoading: Boolean,
    transactions: List<com.familyexpensetracker.data.model.Transaction>,
    categorySummaries: List<com.familyexpensetracker.data.model.CategorySummary>,
    monthSummaries: List<com.familyexpensetracker.data.model.MonthSummary>,
    selectedPeriodTab: PeriodTab,
    yearlyViewMode: YearlyViewMode,
    expandedCategory: String?,
    onCategoryClick: (com.familyexpensetracker.data.model.CategorySummary) -> Unit,
    onBack: () -> Unit,
    navController: NavController,
    onDelete: (com.familyexpensetracker.data.model.Transaction) -> Unit,
    onMonthClick: (com.familyexpensetracker.data.model.DateRange) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                onMonthClick = onMonthClick,
            )
        else -> {
            if (expandedCategory == null) {
                CategorySummaryList(
                    summaries = categorySummaries,
                    onCategoryClick = onCategoryClick,
                )
            } else {
                ExpandedCategoryContent(
                    categoryName = expandedCategory,
                    transactions = transactions,
                    navController = navController,
                    onBack = onBack,
                    onDelete = onDelete,
                )
            }
        }
    }
    }
}

@Composable
private fun ExpandedCategoryContent(
    categoryName: String,
    transactions: List<com.familyexpensetracker.data.model.Transaction>,
    navController: NavController,
    onBack: () -> Unit,
    onDelete: (com.familyexpensetracker.data.model.Transaction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        HorizontalDivider()
        TransactionList(
            transactions = transactions.filter { it.category == categoryName },
            onEdit = { txn -> navController.navigate(NavRoutes.edit(txn.txnId)) },
            onCopy = { txn -> navController.navigate(NavRoutes.copy(txn.txnId)) },
            onDelete = onDelete,
        )
    }
}
