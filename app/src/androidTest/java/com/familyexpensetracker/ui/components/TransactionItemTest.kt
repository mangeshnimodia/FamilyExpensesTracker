package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.Transaction
import org.junit.Rule
import org.junit.Test

class TransactionItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun transactionItem_displaysCorrectInfo() {
        val txn = Transaction(
            txnId = "1",
            date = "2026/06/28",
            amount = -150.5,
            category = "Transport",
            subcategory = "Fuel",
            paymentMethod = "Card",
            description = "Petrol for car",
            account = "Bank"
        )

        composeTestRule.setContent {
            TransactionItem(txn = txn)
        }

        composeTestRule.onNodeWithText("Transport/Fuel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Petrol for car").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amount: ₹150.5 | Account: Bank").assertIsDisplayed()
    }

    @Test
    fun transactionItem_onDeleteTriggered() {
        var deleteClicked = false
        val txn = Transaction(
            txnId = "1",
            date = "2026/06/28",
            amount = -150.5,
            category = "Transport",
            subcategory = "",
            paymentMethod = "Card",
            description = "",
            account = "Bank"
        )

        composeTestRule.setContent {
            TransactionItem(txn = txn, onDelete = { deleteClicked = true })
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        
        assert(deleteClicked)
    }
}
