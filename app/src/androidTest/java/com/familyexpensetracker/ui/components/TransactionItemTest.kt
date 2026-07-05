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
        composeTestRule.onNodeWithText("28/06/2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("Petrol for car").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹150.50").assertIsDisplayed()
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

    @Test
    fun transactionItem_onEditTriggered() {
        var editedTxn: Transaction? = null
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
            TransactionItem(txn = txn, onEdit = { editedTxn = it })
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()
        
        assert(editedTxn == txn)
    }

    @Test
    fun transactionItem_displaysLakhAmountWithIndianGrouping() {
        val txn = Transaction(
            txnId = "2",
            date = "2026/06/28",
            amount = -100000.0,
            category = "Rent",
            subcategory = "",
            paymentMethod = "NEFT",
            description = "",
            account = "Bank"
        )

        composeTestRule.setContent {
            TransactionItem(txn = txn)
        }

        composeTestRule.onNodeWithText("₹1,00,000.00").assertIsDisplayed()
    }

    @Test
    fun transactionItem_onCopyTriggered() {
        var copiedTxn: Transaction? = null
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
            TransactionItem(txn = txn, onCopy = { copiedTxn = it })
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        assert(copiedTxn == txn)
    }
}
