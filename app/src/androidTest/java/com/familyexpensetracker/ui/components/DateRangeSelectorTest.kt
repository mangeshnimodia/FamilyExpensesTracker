package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class DateRangeSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dateRangeSelector_showsOptions() {
        composeTestRule.setContent {
            DateRangeSelector(onRangeSelected = {}, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Day").assertIsDisplayed()
        composeTestRule.onNodeWithText("Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("Financial Year").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Range").assertIsDisplayed()
    }

    @Test
    fun dateRangeSelector_selectingMonthShowsMonthPicker() {
        composeTestRule.setContent {
            DateRangeSelector(onRangeSelected = {}, onDismiss = {})
        }

        composeTestRule.onNodeWithText("Month").performClick()
        
        // Month picker has "Jan", "Feb" etc. (abbreviated in the grid)
        // Actually MonthYearPicker shows full month names but taking first 3 chars? 
        // Let's check MonthYearPicker.kt
        composeTestRule.onNodeWithText("Jan").assertIsDisplayed()
    }
}
