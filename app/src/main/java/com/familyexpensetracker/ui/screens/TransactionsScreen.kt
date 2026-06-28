package com.familyexpensetracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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
import com.familyexpensetracker.ui.components.AppDatePicker
import com.familyexpensetracker.ui.components.TransactionItem
import com.familyexpensetracker.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val totalAmount = transactions.sumOf { it.amount }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    var showDatePicker by remember { mutableStateOf(value = false) }
    val listState = rememberLazyListState()
    val scrollbarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    // Initial set of date if not already set
    LaunchedEffect(Unit) {
        if (selectedDate == null) {
            viewModel.setSelectedDate(Date())
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        AppDatePicker(
            initialDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
        ) {
            showDatePicker = false
        }
    }

    NavHost(navController = navController, startDestination = "main_list") {
        composable("main_list") {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(selectedDate?.let { dateFormatter.format(it) } ?: "Transactions")
                        },
                        actions = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
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
                    if ((selectedDate != null) && (!isLoading)) {
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
                                    text = "Total for Day:",
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
                                    "Tap 🔄 to fetch for this date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithContent {
                                        drawContent()
                                        val firstVisibleElementIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                                        val needScrollbar = listState.layoutInfo.totalItemsCount > listState.layoutInfo.visibleItemsInfo.size

                                        if (needScrollbar && (firstVisibleElementIndex != null)) {
                                            val elementHeight = size.height / listState.layoutInfo.totalItemsCount
                                            val scrollbarHeight = listState.layoutInfo.visibleItemsInfo.size * elementHeight
                                            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight

                                            drawRect(
                                                color = scrollbarColor,
                                                topLeft = Offset(size.width - 4.dp.toPx(), scrollbarOffsetY),
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
                                    ) {
                                        viewModel.deleteTransaction(transaction.txnId)
                                    }
                                }
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
            ) {
                navController.popBackStack()
            }
        }
    }
}
