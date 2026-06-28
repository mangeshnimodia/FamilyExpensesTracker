package com.familyexpensetracker

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_showsSignInButton_whenNotAuthenticated() {
        // Note: This test assumes no existing authorization on the device/emulator
        // In a real scenario, we might need to mock Identity.getAuthorizationClient
        composeTestRule.onNodeWithText("Sign In with Google").assertExists()
    }
}
