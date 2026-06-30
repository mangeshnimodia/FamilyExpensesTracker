package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.ui.screens.PeriodTab
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class PeriodTabBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun periodTabBar_showsAllFourTabs() {
        composeTestRule.setContent {
            PeriodTabBar(selectedTab = PeriodTab.Daily, onTabSelected = {})
        }

        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yearly").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun periodTabBar_dailyTabSelectedByDefault() {
        composeTestRule.setContent {
            PeriodTabBar(selectedTab = PeriodTab.Daily, onTabSelected = {})
        }

        composeTestRule.onNodeWithText("Daily").assertIsSelected()
    }

    @Test
    fun periodTabBar_monthlyTabSelectedWhenSet() {
        composeTestRule.setContent {
            PeriodTabBar(selectedTab = PeriodTab.Monthly, onTabSelected = {})
        }

        composeTestRule.onNodeWithText("Monthly").assertIsSelected()
    }

    @Test
    fun periodTabBar_clickingTabCallsCallback() {
        val onTabSelected: (PeriodTab) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            PeriodTabBar(selectedTab = PeriodTab.Daily, onTabSelected = onTabSelected)
        }

        composeTestRule.onNodeWithText("Monthly").performClick()

        verify { onTabSelected(PeriodTab.Monthly) }
    }

    @Test
    fun periodTabBar_clickingAllTabCallsCallback() {
        val onTabSelected: (PeriodTab) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            PeriodTabBar(selectedTab = PeriodTab.Daily, onTabSelected = onTabSelected)
        }

        composeTestRule.onNodeWithText("All").performClick()

        verify { onTabSelected(PeriodTab.All) }
    }
}
