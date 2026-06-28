package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.Transaction
import org.junit.Rule
import org.junit.Test

class GroupedTransactionsViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun groupedTransactionsView_showsAccounts() {
        val transactions = listOf(
            Transaction(txnId = "1", date = "2026/06/28", amount = -100.0, category = "Food", subcategory = "Groceries", paymentMethod = "Cash", description = "Milk", account = "Savings"),
            Transaction(txnId = "2", date = "2026/06/28", amount = -50.0, category = "Transport", subcategory = "", paymentMethod = "Card", description = "Bus", account = "Cash")
        )

        composeTestRule.setContent {
            GroupedTransactionsView(transactions = transactions, onDelete = {})
        }

        composeTestRule.onNodeWithText("Savings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cash").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtotal: ₹100.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Subtotal: ₹50.00").assertIsDisplayed()
    }

    @Test
    fun groupedTransactionsView_expandsAccountToCategories() {
        val transactions = listOf(
            Transaction(txnId = "1", date = "2026/06/28", amount = -100.0, category = "Food", subcategory = "Groceries", paymentMethod = "Cash", description = "Milk", account = "Savings")
        )

        composeTestRule.setContent {
            GroupedTransactionsView(transactions = transactions, onDelete = {})
        }

        // Initially categories are not visible
        composeTestRule.onNodeWithText("Food").assertDoesNotExist()

        // Expand account
        composeTestRule.onNodeWithText("Savings").performClick()
        
        // Now category should be visible
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: ₹100.00").assertIsDisplayed()
    }

    @Test
    fun groupedTransactionsView_expandsCategoryToTransactions() {
        val transactions = listOf(
            Transaction(txnId = "1", date = "2026/06/28", amount = -100.0, category = "Food", subcategory = "Groceries", paymentMethod = "Cash", description = "Milk", account = "Savings")
        )

        composeTestRule.setContent {
            GroupedTransactionsView(transactions = transactions, onDelete = {})
        }

        // Expand account then category
        composeTestRule.onNodeWithText("Savings").performClick()
        composeTestRule.onNodeWithText("Food").performClick()
        
        // Now transaction details should be visible (e.g. description)
        composeTestRule.onNodeWithText("Milk").assertIsDisplayed()
    }
}
