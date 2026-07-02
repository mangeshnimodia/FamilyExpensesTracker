package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import java.util.Date

class AppDatePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appDatePicker_showsOkButton() {
        composeTestRule.setContent {
            AppDatePicker(
                initialDate = Date(),
                onDateSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }

    @Test
    fun appDatePicker_showsCancelButton() {
        composeTestRule.setContent {
            AppDatePicker(
                initialDate = Date(),
                onDateSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun appDatePicker_cancelButtonCallsOnDismiss() {
        val onDismiss: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AppDatePicker(
                initialDate = Date(),
                onDateSelected = {},
                onDismiss = onDismiss,
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        verify { onDismiss() }
    }

    @Test
    fun appDatePicker_okButtonCallsOnDismiss() {
        val onDismiss: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AppDatePicker(
                initialDate = Date(),
                onDateSelected = {},
                onDismiss = onDismiss,
            )
        }

        composeTestRule.onNodeWithText("OK").performClick()

        verify { onDismiss() }
    }

    @Test
    fun appDatePicker_okButtonCallsOnDateSelectedWithADate() {
        var selectedDate: Date? = null

        composeTestRule.setContent {
            AppDatePicker(
                initialDate = Date(),
                onDateSelected = { selectedDate = it },
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("OK").performClick()

        assert(selectedDate != null) { "Expected onDateSelected to be called with a Date" }
    }

    @Test
    fun appDatePicker_nullInitialDate_stillDisplays() {
        composeTestRule.setContent {
            AppDatePicker(
                initialDate = null,
                onDateSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }
}
