package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AccountBalanceTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accountBalanceText_nullBalance_showsZero() {
        composeTestRule.setContent {
            AccountBalanceText(balance = null)
        }

        composeTestRule.onNodeWithTag("accountBalanceText").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_nullBalance_showsBalanceLabel() {
        composeTestRule.setContent {
            AccountBalanceText(balance = null)
        }

        composeTestRule.onNodeWithText("Balance").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_positiveBalance_displaysAmount() {
        composeTestRule.setContent {
            AccountBalanceText(balance = 1500.75)
        }

        composeTestRule.onNodeWithText("₹1,500.75").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_negativeBalance_displaysAbsoluteValue() {
        composeTestRule.setContent {
            AccountBalanceText(balance = -320.50)
        }

        composeTestRule.onNodeWithText("₹320.50").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_zeroBalance_displaysZero() {
        composeTestRule.setContent {
            AccountBalanceText(balance = 0.0)
        }

        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_isAlwaysDisplayed_regardlessOfBalance() {
        composeTestRule.setContent {
            AccountBalanceText(balance = null)
        }

        composeTestRule.onNodeWithTag("accountBalanceText").assertIsDisplayed()
    }

    @Test
    fun accountBalanceText_showsBalanceLabelText() {
        composeTestRule.setContent {
            AccountBalanceText(balance = 500.0)
        }

        composeTestRule.onNodeWithText("Balance").assertIsDisplayed()
    }
}
