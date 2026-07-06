package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.ui.theme.AppColors
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class CategorySummaryListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun makeSummary(
        category: String,
        amount: Double,
        count: Int,
    ) = CategorySummary(
        category = category,
        totalAmount = amount,
        transactionCount = count,
        subcategories = emptyList(),
    )

    @Test
    fun categorySummaryList_showsCategoryName() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(makeSummary("Food", -500.0, 3)),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun categorySummaryList_showsTransactionCount() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(makeSummary("Food", -500.0, 3)),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("3 transactions").assertIsDisplayed()
    }

    @Test
    fun categorySummaryList_showsAmount() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(makeSummary("Food", -500.0, 3)),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("₹500.00").assertIsDisplayed()
        val color = composeTestRule.onNodeWithText("₹500.00")
            .fetchSemanticsNode().config[AmountColorSemanticsKey]
        assert(color == AppColors.expenseRed) { "Expense category amount must be red, got $color" }
    }

    @Test
    fun categorySummaryList_showsMultipleCategories() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(
                    makeSummary("Food", -500.0, 3),
                    makeSummary("Transport", -200.0, 2),
                ),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transport").assertIsDisplayed()
    }

    @Test
    fun categorySummaryList_clickingRowCallsCallback() {
        val summary = makeSummary("Food", -500.0, 3)
        val onCategoryClick: (CategorySummary) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(summary),
                onCategoryClick = onCategoryClick,
            )
        }

        composeTestRule.onNodeWithText("Food").performClick()

        verify { onCategoryClick(summary) }
    }

    @Test
    fun categorySummaryList_singularTransactionCount() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(makeSummary("Rent", -1000.0, 1)),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("1 transactions").assertIsDisplayed()
    }

    @Test
    fun categorySummaryRow_displaysRupeePrefix() {
        composeTestRule.setContent {
            CategorySummaryList(
                summaries = listOf(makeSummary("Food", -500.0, 3)),
                onCategoryClick = {},
            )
        }

        composeTestRule.onNodeWithText("\u20B9500.00").assertIsDisplayed()
    }
}
