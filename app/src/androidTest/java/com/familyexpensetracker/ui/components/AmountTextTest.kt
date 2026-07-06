package com.familyexpensetracker.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.ui.theme.AppColors
import org.junit.Rule
import org.junit.Test

class AmountTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun amountText_positiveAmount_displaysFormattedText() {
        composeTestRule.setContent {
            AmountText(amount = 1500.75)
        }

        composeTestRule.onNodeWithText("₹1,500.75").assertIsDisplayed()
    }

    @Test
    fun amountText_negativeAmount_displaysSignedText() {
        composeTestRule.setContent {
            AmountText(amount = -500.0)
        }

        composeTestRule.onNodeWithText("₹500.00").assertIsDisplayed()
    }

    @Test
    fun amountText_zero_displaysZero() {
        composeTestRule.setContent {
            AmountText(amount = 0.0)
        }

        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
    }

    @Test
    fun amountText_positiveAmount_usesIncomeGreenColor() {
        var capturedColor: Color? = null
        composeTestRule.setContent {
            AmountText(
                amount = 1000.0,
                onColorCaptured = { capturedColor = it },
            )
        }

        composeTestRule.onNodeWithText("₹1,000.00").assertIsDisplayed()
        assert(capturedColor == AppColors.incomeGreen) {
            "Positive amount must use AppColors.incomeGreen, got $capturedColor"
        }
    }

    @Test
    fun amountText_negativeAmount_usesExpenseRedColor() {
        var capturedColor: Color? = null
        composeTestRule.setContent {
            AmountText(
                amount = -200.0,
                onColorCaptured = { capturedColor = it },
            )
        }

        composeTestRule.onNodeWithText("₹200.00").assertIsDisplayed()
        assert(capturedColor == AppColors.expenseRed) {
            "Negative amount must use AppColors.expenseRed, got $capturedColor"
        }
    }

    @Test
    fun amountText_negativeLakh_displaysSignedIndianGrouping() {
        composeTestRule.setContent {
            AmountText(amount = -100000.0)
        }

        composeTestRule.onNodeWithText("₹1,00,000.00").assertIsDisplayed()
    }
}
