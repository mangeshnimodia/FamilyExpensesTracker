package com.familyexpensetracker.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.SubcategoryGroup
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.ui.viewmodel.TransactionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubcategoryDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val transactionVM = mockk<TransactionViewModel>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)

    private val txn1 = Transaction(
        txnId = "1",
        date = "15/06/2026",
        amount = -200.0,
        category = "Food",
        subcategory = "Groceries",
        account = "Bank",
    )

    private val txn2 = Transaction(
        txnId = "2",
        date = "20/06/2026",
        amount = -150.0,
        category = "Food",
        subcategory = "",
        account = "Cash",
    )

    private val categorySummary = CategorySummary(
        category = "Food",
        totalAmount = -350.0,
        transactionCount = 2,
        subcategories = listOf(
            SubcategoryGroup(
                subcategory = "Groceries",
                totalAmount = -200.0,
                transactionCount = 1,
                transactions = listOf(txn1),
            ),
            SubcategoryGroup(
                subcategory = "",
                totalAmount = -150.0,
                transactionCount = 1,
                transactions = listOf(txn2),
            ),
        ),
    )

    @Before
    fun setUp() {
        every { transactionVM.categorySummaries } returns MutableStateFlow(listOf(categorySummary))
    }

    @Test
    fun subcategoryDetailScreen_showsCategoryInTopBar() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun subcategoryDetailScreen_showsSubcategoryRow() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithText("Groceries").assertIsDisplayed()
    }

    @Test
    fun subcategoryDetailScreen_showsBlankSubcategoryAsNone() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithText("(none)").assertIsDisplayed()
    }

    @Test
    fun subcategoryDetailScreen_showsTransactionCountForSubcategory() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onAllNodesWithText("1 transactions").assertCountEquals(2)
    }

    @Test
    fun subcategoryDetailScreen_expandingSubcategoryShowsTransactions() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithText("Groceries").performClick()

        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
    }

    @Test
    fun subcategoryDetailScreen_backButtonNavigatesBack() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        verify { navController.popBackStack() }
    }

    @Test
    fun subcategoryDetailScreen_showsSubcategoryAmount() {
        composeTestRule.setContent {
            SubcategoryDetailScreen(
                transactionVM = transactionVM,
                category = "Food",
                navController = navController,
            )
        }

        composeTestRule.onNodeWithText("₹200.00").assertIsDisplayed()
    }
}
