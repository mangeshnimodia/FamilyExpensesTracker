package com.familyexpensetracker.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class TotalBannerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun totalBanner_displaysFormattedAmount() {
        composeTestRule.setContent {
            TotalBanner(total = 1200.0, totalColor = Color.Green)
        }

        composeTestRule.onNodeWithText("₹1,200.00").assertIsDisplayed()
    }

    @Test
    fun totalBanner_displaysAbsoluteValueForNegativeTotal() {
        composeTestRule.setContent {
            TotalBanner(total = -750.50, totalColor = Color.Red)
        }

        composeTestRule.onNodeWithText("₹750.50").assertIsDisplayed()
    }

    @Test
    fun totalBanner_displaysZeroTotal() {
        composeTestRule.setContent {
            TotalBanner(total = 0.0, totalColor = Color.Gray)
        }

        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
    }

    @Test
    fun totalBanner_hasNoNavigationArrows() {
        composeTestRule.setContent {
            TotalBanner(total = 500.0, totalColor = Color.Green)
        }

        composeTestRule.onNodeWithText("<").assertDoesNotExist()
        composeTestRule.onNodeWithText(">").assertDoesNotExist()
    }

    @Test
    fun totalBanner_isDisplayed() {
        composeTestRule.setContent {
            TotalBanner(total = 100.0, totalColor = Color.Green)
        }

        composeTestRule.onNodeWithText("₹100.00").assertIsDisplayed()
    }
}
