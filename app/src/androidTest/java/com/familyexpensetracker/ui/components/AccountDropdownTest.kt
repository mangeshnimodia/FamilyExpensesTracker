package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class AccountDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountDropdown_showsAllAccountsByDefault() {
        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank", "Cash"),
                selectedAccount = null,
                onAccountSelected = {},
            )
        }

        composeTestRule.onNodeWithText("All Accounts").assertIsDisplayed()
    }

    @Test
    fun accountDropdown_chipNotSelectedWhenNoAccount() {
        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank"),
                selectedAccount = null,
                onAccountSelected = {},
            )
        }

        composeTestRule.onNodeWithText("All Accounts").assertIsNotSelected()
    }

    @Test
    fun accountDropdown_chipSelectedWhenAccountChosen() {
        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank", "Cash"),
                selectedAccount = "Bank",
                onAccountSelected = {},
            )
        }

        composeTestRule.onNodeWithText("Bank").assertIsSelected()
    }

    @Test
    fun accountDropdown_clickChipShowsDropdown() {
        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank", "Cash"),
                selectedAccount = null,
                onAccountSelected = {},
            )
        }

        composeTestRule.onNodeWithText("All Accounts").performClick()

        composeTestRule.onNodeWithText("Bank").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cash").assertIsDisplayed()
    }

    @Test
    fun accountDropdown_selectingAccountCallsCallbackWithAccount() {
        val onAccountSelected: (String?) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank", "Cash"),
                selectedAccount = null,
                onAccountSelected = onAccountSelected,
            )
        }

        composeTestRule.onNodeWithText("All Accounts").performClick()
        composeTestRule.onNodeWithText("Bank").performClick()

        verify { onAccountSelected("Bank") }
    }

    @Test
    fun accountDropdown_selectingAllAccountsCallsCallbackWithNull() {
        val onAccountSelected: (String?) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AccountDropdown(
                accounts = listOf("Bank", "Cash"),
                selectedAccount = "Bank",
                onAccountSelected = onAccountSelected,
            )
        }

        composeTestRule.onNodeWithText("Bank").performClick()
        composeTestRule.onNodeWithText("All Accounts").performClick()

        verify { onAccountSelected(null) }
    }
}
