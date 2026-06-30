package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.FilterType
import com.familyexpensetracker.ui.components.*
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

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

    val totalAmount = transactions.sumOf { it.amount }
    val snackbarHostState = remember { SnackbarHostState() }
    val dateRangeFormatter = remember { DateRangeFormatter() }
    val periodNavigator = remember { PeriodNavigator() }
    var yearlyViewMode by remember { mutableStateOf(YearlyViewMode.Category) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
    }

    fun defaultDateRangeForTab(tab: PeriodTab): DateRange {
        val now = Calendar.getInstance()
        return when (tab) {
            PeriodTab.Daily -> DateRange.Day(now.time)
            PeriodTab.Monthly -> DateRange.Month(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
            PeriodTab.Yearly -> {
                val fyStart = if (now.get(Calendar.MONTH) >= Calendar.APRIL) now.get(Calendar.YEAR) else now.get(Calendar.YEAR) - 1
                DateRange.FinancialYear(fyStart)
            }
            PeriodTab.All -> DateRange.All
        }
    }

    NavHost(navController = navController, startDestination = "main_list") {
        composable("main_list") {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text("Family Expenses", style = MaterialTheme.typography.titleMedium) },
                        actions = {
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
                                }
                            )
                            IconButton(onClick = { (context as? Activity)?.finish() }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Exit")
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
                    PeriodTabBar(
                        selectedTab = selectedPeriodTab,
                        onTabSelected = { tab ->
                            viewModel.setSelectedPeriodTab(tab)
                            viewModel.setSelectedDateRange(defaultDateRangeForTab(tab))
                        }
                    )

                    if (selectedPeriodTab != PeriodTab.All) {
                        val navBarTotal = when (selectedFilter.type) {
                            FilterType.EXPENSE -> expenseTotal
                            FilterType.INCOME -> transactions.filter { it.amount > 0 }.sumOf { it.amount }
                            FilterType.BALANCE -> totalAmount
                        }
                        val navBarColor = when (selectedFilter.type) {
                            FilterType.INCOME -> Color(0xFF4CAF50)
                            else -> if (navBarTotal < 0) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                        }
                        PeriodNavBar(
                            dateRange = selectedDateRange,
                            label = dateRangeFormatter.format(selectedDateRange),
                            onPrevious = { viewModel.setSelectedDateRange(periodNavigator.previous(selectedDateRange)) },
                            onNext = { viewModel.setSelectedDateRange(periodNavigator.next(selectedDateRange)) },
                            navigator = periodNavigator,
                            expenseTotal = navBarTotal,
                            totalColor = navBarColor,
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
                            else ->
                                CategorySummaryList(
                                    summaries = categorySummaries,
                                    onCategoryClick = { summary ->
                                        navController.navigate("subcategory/${summary.category}")
                                    }
                                )
                        }

                        FloatingActionButton(
                            onClick = { viewModel.fetchTransactions() },
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
        composable("add") {
            AddExpenseScreen(viewModel = viewModel, onDismiss = { navController.popBackStack() })
        }
        composable("edit/{txnId}") { backStackEntry ->
            val txnId = backStackEntry.arguments?.getString("txnId")
            val transaction = transactions.find { it.txnId == txnId }
            AddExpenseScreen(viewModel = viewModel, onDismiss = { navController.popBackStack() }, transactionToEdit = transaction)
        }
        composable("subcategory/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            SubcategoryDetailScreen(viewModel = viewModel, category = category, navController = navController)
        }
    }
}
