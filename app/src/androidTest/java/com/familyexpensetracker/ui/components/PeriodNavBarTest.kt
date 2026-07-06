package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import java.util.Date

class PeriodNavBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun periodNavBar_showsLabel() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "30 Jun 2026",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("30 Jun 2026").assertIsDisplayed()
    }

    @Test
    fun periodNavBar_showsTotal() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
                expenseTotal = 1234.50,
            )
        }

        composeTestRule.onNodeWithText("₹1,234.50").assertIsDisplayed()
    }

    @Test
    fun periodNavBar_showsNavigationButtons() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("<").assertIsDisplayed()
        composeTestRule.onNodeWithText(">").assertIsDisplayed()
    }

    @Test
    fun periodNavBar_buttonsEnabledForNavigableRange() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("<").assertIsEnabled()
        composeTestRule.onNodeWithText(">").assertIsEnabled()
    }

    @Test
    fun periodNavBar_buttonsDisabledForAllRange() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.All,
                label = "All",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("<").assertIsNotEnabled()
        composeTestRule.onNodeWithText(">").assertIsNotEnabled()
    }

    @Test
    fun periodNavBar_previousButtonCallsCallback() {
        val onPrevious: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = onPrevious,
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("<").performClick()

        verify { onPrevious() }
    }

    @Test
    fun periodNavBar_nextButtonCallsCallback() {
        val onNext: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = onNext,
            )
        }

        composeTestRule.onNodeWithText(">").performClick()

        verify { onNext() }
    }

    @Test
    fun periodNavBar_showsZeroTotalByDefault() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
    }

    @Test
    fun periodNavBar_showsNegativeTotalWithSignedAmount() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
                expenseTotal = -500.75,
            )
        }

        composeTestRule.onNodeWithText("₹500.75").assertIsDisplayed()
    }
}
