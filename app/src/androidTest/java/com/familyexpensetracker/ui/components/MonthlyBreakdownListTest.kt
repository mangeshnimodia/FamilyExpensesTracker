package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.DateRange
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.ui.theme.AppColors
import io.mockk.mockk
import io.mockk.verify
import java.util.Calendar
import org.junit.Rule
import org.junit.Test

class MonthlyBreakdownListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeSummary(year: Int, month: Int, amount: Double, count: Int = 1) =
        MonthSummary(year = year, month = month, totalAmount = amount, transactionCount = count)

    @Test
    fun monthlyBreakdownList_showsMonthLabel() {
        // month=5 is June (Calendar.JUNE), year=2026
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.JUNE, -500.0)),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("June 2026").assertIsDisplayed()
    }

    @Test
    fun monthlyBreakdownList_showsAmount() {
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.JUNE, -500.0)),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("₹500.00").assertIsDisplayed()
        val color = composeTestRule.onNodeWithText("₹500.00")
            .fetchSemanticsNode().config[AmountColorSemanticsKey]
        assert(color == AppColors.expenseRed) { "Expense month amount must be red, got $color" }
    }

    @Test
    fun monthlyBreakdownList_showsMultipleMonths() {
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(
                    makeSummary(2026, Calendar.JUNE, -500.0),
                    makeSummary(2026, Calendar.MAY, -300.0),
                ),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("June 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("May 2026").assertIsDisplayed()
    }

    @Test
    fun monthlyBreakdownList_clickingRowCallsCallbackWithCorrectMonth() {
        val onMonthClick: (DateRange.Month) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.JUNE, -500.0)),
                onMonthClick = onMonthClick,
            )
        }

        composeTestRule.onNodeWithText("June 2026").performClick()

        verify { onMonthClick(DateRange.Month(2026, Calendar.JUNE)) }
    }

    @Test
    fun monthlyBreakdownList_showsAprilLabelCorrectly() {
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.APRIL, -1200.0)),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("April 2026").assertIsDisplayed()
    }

    @Test
    fun monthlyBreakdownList_showsPositiveAmountForIncome() {
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.JUNE, 2500.0)),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("\u20B92,500.00").assertIsDisplayed()
        val color = composeTestRule.onNodeWithText("\u20B92,500.00")
            .fetchSemanticsNode().config[AmountColorSemanticsKey]
        assert(color == AppColors.incomeGreen) { "Income month amount must be green, got $color" }
    }

    @Test
    fun monthlySummaryRow_displaysRupeePrefix() {
        composeTestRule.setContent {
            MonthlyBreakdownList(
                summaries = listOf(makeSummary(2026, Calendar.JUNE, -500.0)),
                onMonthClick = {},
            )
        }

        composeTestRule.onNodeWithText("\u20B9500.00").assertIsDisplayed()
    }
}
