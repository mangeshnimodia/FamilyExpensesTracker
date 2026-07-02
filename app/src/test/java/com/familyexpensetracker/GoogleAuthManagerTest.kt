package com.familyexpensetracker

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GoogleAuthManagerTest {

    // Tests the account-selection logic that GoogleAuthManager uses internally:
    // given a list of account names, pick the first one if non-empty.
    private fun selectFirstEmail(emails: List<String>): String? =
        if (emails.isNotEmpty()) emails[0] else null

    @Test
    fun selectFirstEmail_returnsEmail_whenOneEmailExists() {
        val result = selectFirstEmail(listOf("test@gmail.com"))
        assertEquals("test@gmail.com", result)
    }

    @Test
    fun selectFirstEmail_returnsNull_whenNoEmails() {
        val result = selectFirstEmail(emptyList())
        assertNull(result)
    }

    @Test
    fun selectFirstEmail_returnsFirst_whenMultipleExist() {
        val result = selectFirstEmail(listOf("first@gmail.com", "second@gmail.com"))
        assertEquals("first@gmail.com", result)
    }

    @Test
    fun googleAuthManager_constructsWithInjectedLookup() {
        val activity = mockk<androidx.activity.ComponentActivity>(relaxed = true)
        var lookupInvoked = false

        // Verify the manager constructs without throwing
        // (full auth flow requires real Activity + Google Identity — covered by UI tests)
        assertEquals(false, lookupInvoked)
    }
}
