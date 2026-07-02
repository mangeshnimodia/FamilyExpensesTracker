package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class CustomRangePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun customRangePicker_showsOkButton() {
        composeTestRule.setContent {
            CustomRangePicker(
                onRangeSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun customRangePicker_showsCancelButton() {
        composeTestRule.setContent {
            CustomRangePicker(
                onRangeSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun customRangePicker_showsSelectDateRangeTitle() {
        composeTestRule.setContent {
            CustomRangePicker(
                onRangeSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Select Date Range").assertIsDisplayed()
    }

    @Test
    fun customRangePicker_cancelButtonCallsOnDismiss() {
        val onDismiss: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            CustomRangePicker(
                onRangeSelected = { _, _ -> },
                onDismiss = onDismiss,
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        verify { onDismiss() }
    }

    @Test
    fun customRangePicker_okDisabledWhenNoDatesSelected() {
        composeTestRule.setContent {
            CustomRangePicker(
                onRangeSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("OK").assertIsNotEnabled()
    }
}
