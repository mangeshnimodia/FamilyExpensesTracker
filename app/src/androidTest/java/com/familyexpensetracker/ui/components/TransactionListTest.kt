package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.Transaction
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class TransactionListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleTransaction = Transaction(
        txnId = "txn-1",
        date = "2026/06/15",
        amount = -150.0,
        category = "Food",
        subcategory = "Groceries",
        paymentMethod = "Cash",
        description = "Weekly shop",
        account = "Passbook",
    )

    @Test
    fun transactionList_showsTransactionItem() {
        composeTestRule.setContent {
            TransactionList(
                transactions = listOf(sampleTransaction),
                onEdit = {},
                onCopy = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹150.00").assertIsDisplayed() // negative amount shown without sign, color indicates direction
    }

    @Test
    fun transactionList_showsMultipleTransactions() {
        val txn2 = sampleTransaction.copy(txnId = "txn-2", category = "Transport", subcategory = "", amount = -50.0)
        composeTestRule.setContent {
            TransactionList(
                transactions = listOf(sampleTransaction, txn2),
                onEdit = {},
                onCopy = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithText("Food/Groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transport").assertIsDisplayed()
    }

    @Test
    fun transactionList_emptyList_showsNothing() {
        composeTestRule.setContent {
            TransactionList(
                transactions = emptyList(),
                onEdit = {},
                onCopy = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertDoesNotExist()
    }

    @Test
    fun transactionList_onEditTriggered() {
        val onEdit: (Transaction) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            TransactionList(
                transactions = listOf(sampleTransaction),
                onEdit = onEdit,
                onCopy = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        verify { onEdit(sampleTransaction) }
    }

    @Test
    fun transactionList_onCopyTriggered() {
        val onCopy: (Transaction) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            TransactionList(
                transactions = listOf(sampleTransaction),
                onEdit = {},
                onCopy = onCopy,
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Copy").performClick()

        verify { onCopy(sampleTransaction) }
    }

    @Test
    fun transactionList_onDeleteTriggered() {
        val onDelete: (Transaction) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            TransactionList(
                transactions = listOf(sampleTransaction),
                onEdit = {},
                onCopy = {},
                onDelete = onDelete,
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        verify { onDelete(sampleTransaction) }
    }
}
