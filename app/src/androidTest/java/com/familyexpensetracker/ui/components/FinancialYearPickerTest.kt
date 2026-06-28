package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.*

class FinancialYearPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun financialYearPicker_showsYears() {
        composeTestRule.setContent {
            FinancialYearPickerDialog(
                onYearSelected = {},
                onDismiss = {}
            )
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startYearShort = currentYear % 100
        val endYearShort = (currentYear + 1) % 100
        val expectedDisplay = "FY%02d-%02d".format(startYearShort, endYearShort)

        composeTestRule.onNodeWithText(expectedDisplay).assertIsDisplayed()
    }

    @Test
    fun financialYearPicker_selectingYearTriggersCallback() {
        var selectedYear = -1
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        
        composeTestRule.setContent {
            FinancialYearPickerDialog(
                onYearSelected = { selectedYear = it },
                onDismiss = {}
            )
        }

        val startYearShort = currentYear % 100
        val endYearShort = (currentYear + 1) % 100
        val display = "FY%02d-%02d".format(startYearShort, endYearShort)

        composeTestRule.onNodeWithText(display).performClick()
        
        assert(selectedYear == currentYear)
    }
}
