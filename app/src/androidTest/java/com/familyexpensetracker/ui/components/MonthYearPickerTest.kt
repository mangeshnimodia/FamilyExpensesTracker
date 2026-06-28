package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.*

class MonthYearPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun monthYearPicker_showsCorrectInitialYear() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
        
        composeTestRule.setContent {
            MonthYearPickerDialog(
                onMonthSelected = { _, _ -> },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithText(currentYear).assertIsDisplayed()
    }

    @Test
    fun monthYearPicker_canChangeYear() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        composeTestRule.setContent {
            MonthYearPickerDialog(
                onMonthSelected = { _, _ -> },
                onDismiss = {}
            )
        }

        // Click next year
        composeTestRule.onNodeWithContentDescription("Next Year").performClick()
        composeTestRule.onNodeWithText((currentYear + 1).toString()).assertIsDisplayed()

        // Click previous year twice
        composeTestRule.onNodeWithContentDescription("Previous Year").performClick()
        composeTestRule.onNodeWithContentDescription("Previous Year").performClick()
        composeTestRule.onNodeWithText((currentYear - 1).toString()).assertIsDisplayed()
    }

    @Test
    fun monthYearPicker_selectingMonthTriggersCallback() {
        var selectedYear = -1
        var selectedMonth = -1
        
        composeTestRule.setContent {
            MonthYearPickerDialog(
                onMonthSelected = { y, m -> 
                    selectedYear = y
                    selectedMonth = m
                },
                onDismiss = {}
            )
        }

        // Click "Jan" (should be index 0)
        composeTestRule.onNodeWithText("Jan").performClick()
        
        assert(selectedMonth == 0)
        assert(selectedYear == Calendar.getInstance().get(Calendar.YEAR))
    }
}
