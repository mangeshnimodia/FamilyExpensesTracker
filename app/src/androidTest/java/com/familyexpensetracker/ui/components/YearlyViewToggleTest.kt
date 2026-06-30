package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class YearlyViewToggleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun yearlyViewToggle_showsBothTabs() {
        composeTestRule.setContent {
            YearlyViewToggle(selected = YearlyViewMode.Date, onSelected = {})
        }

        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun yearlyViewToggle_dateTabSelectedByDefault() {
        composeTestRule.setContent {
            YearlyViewToggle(selected = YearlyViewMode.Date, onSelected = {})
        }

        composeTestRule.onNodeWithText("Date").assertIsSelected()
    }

    @Test
    fun yearlyViewToggle_categoryTabSelectedWhenSet() {
        composeTestRule.setContent {
            YearlyViewToggle(selected = YearlyViewMode.Category, onSelected = {})
        }

        composeTestRule.onNodeWithText("Category").assertIsSelected()
    }

    @Test
    fun yearlyViewToggle_clickingCategoryCallsCallback() {
        val onSelected: (YearlyViewMode) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            YearlyViewToggle(selected = YearlyViewMode.Date, onSelected = onSelected)
        }

        composeTestRule.onNodeWithText("Category").performClick()

        verify { onSelected(YearlyViewMode.Category) }
    }

    @Test
    fun yearlyViewToggle_clickingDateCallsCallback() {
        val onSelected: (YearlyViewMode) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            YearlyViewToggle(selected = YearlyViewMode.Category, onSelected = onSelected)
        }

        composeTestRule.onNodeWithText("Date").performClick()

        verify { onSelected(YearlyViewMode.Date) }
    }
}
