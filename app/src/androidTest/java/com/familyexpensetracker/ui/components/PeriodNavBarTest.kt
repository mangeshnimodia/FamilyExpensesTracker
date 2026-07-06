package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.ui.theme.AppColors
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
        val color = composeTestRule.onNodeWithText("₹1,234.50")
            .fetchSemanticsNode().config[AmountColorSemanticsKey]
        assert(color == AppColors.incomeGreen) { "Positive expense total must be green, got $color" }
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
    fun periodNavBar_showsDatePickerIconForDayRange() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
                onDateSelected = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Pick date").assertIsDisplayed()
    }

    @Test
    fun periodNavBar_noDatePickerIconForMonthRange() {
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Month(2026, 5),
                label = "June 2026",
                onPrevious = {},
                onNext = {},
            )
        }

        composeTestRule.onNodeWithContentDescription("Pick date").assertDoesNotExist()
    }

    @Test
    fun periodNavBar_datePickerIcon_callsOnDateSelected() {
        var selectedDate: Date? = null
        composeTestRule.setContent {
            PeriodNavBar(
                dateRange = DateRange.Day(Date()),
                label = "Today",
                onPrevious = {},
                onNext = {},
                onDateSelected = { selectedDate = it },
            )
        }

        composeTestRule.onNodeWithContentDescription("Pick date").assertIsDisplayed()
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
        val color = composeTestRule.onNodeWithText("₹500.75")
            .fetchSemanticsNode().config[AmountColorSemanticsKey]
        assert(color == AppColors.expenseRed) { "Negative expense total must be red, got $color" }
    }
}
