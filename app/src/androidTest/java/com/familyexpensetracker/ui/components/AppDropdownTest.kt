package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class AppDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appDropdown_displaysLabel() {
        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = false,
                onExpandedChange = {},
                onValueSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun appDropdown_displaysSelectedValue() {
        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "Food",
                options = listOf("Food", "Transport"),
                expanded = false,
                onExpandedChange = {},
                onValueSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun appDropdown_showsOptionsWhenExpanded() {
        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = true,
                onExpandedChange = {},
                onValueSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transport").assertIsDisplayed()
    }

    @Test
    fun appDropdown_callsOnExpandedChangeWhenClicked() {
        val onExpandedChange: (Boolean) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = false,
                onExpandedChange = onExpandedChange,
                onValueSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Category").performClick()

        verify { onExpandedChange(any()) }
    }

    @Test
    fun appDropdown_callsOnValueSelectedAndOnDismissWhenItemClicked() {
        val onValueSelected: (String) -> Unit = mockk(relaxed = true)
        val onDismiss: () -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = true,
                onExpandedChange = {},
                onValueSelected = onValueSelected,
                onDismiss = onDismiss,
            )
        }

        composeTestRule.onNodeWithText("Food").performClick()

        verify { onValueSelected("Food") }
        verify { onDismiss() }
    }

    @Test
    fun appDropdown_doesNotShowOptionsWhenCollapsed() {
        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = false,
                onExpandedChange = {},
                onValueSelected = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("Food").assertDoesNotExist()
        composeTestRule.onNodeWithText("Transport").assertDoesNotExist()
    }

    @Test
    fun appDropdown_disabledDoesNotRespondToClick() {
        val onExpandedChange: (Boolean) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            AppDropdown(
                label = "Category",
                selectedValue = "",
                options = listOf("Food", "Transport"),
                expanded = false,
                onExpandedChange = onExpandedChange,
                onValueSelected = {},
                onDismiss = {},
                enabled = false,
            )
        }

        composeTestRule.onNodeWithText("Category").performClick()

        verify(exactly = 0) { onExpandedChange(any()) }
    }
}
