package com.familyexpensetracker

import android.accounts.Account
import android.content.Context
import androidx.activity.ComponentActivity
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleAuthManagerTest {

    @Test
    fun googleAuthManager_constructsWithoutThrowing() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        var emailReadyCalled = false
        GoogleAuthManager(activity = activity, onEmailReady = { emailReadyCalled = true })
        assertFalse(emailReadyCalled)
    }

    @Test
    fun checkExisting_invokesGetGoogleAccounts() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val context = mockk<Context>()
        var lambdaInvoked = false
        val manager = GoogleAuthManager(
            activity = activity,
            onEmailReady = {},
            getGoogleAccounts = { lambdaInvoked = true; emptyArray() },
        )
        manager.checkExisting(context)
        assertTrue(lambdaInvoked)
    }

    @Test
    fun checkExisting_doesNotCallOnEmailReady_whenNoAccounts() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val context = mockk<Context>()
        var emailReadyCalled = false
        val manager = GoogleAuthManager(
            activity = activity,
            onEmailReady = { emailReadyCalled = true },
            getGoogleAccounts = { emptyArray() },
        )
        manager.checkExisting(context)
        assertFalse(emailReadyCalled)
    }

    @Test
    fun checkExisting_invokesGetGoogleAccounts_whenMultipleExist() {
        val activity = mockk<ComponentActivity>(relaxed = true)
        val context = mockk<Context>()
        var invoked = false
        // Supply two real Account objects — production code selects accounts[0] internally
        val accounts = arrayOf(
            Account("first@gmail.com", "com.google"),
            Account("second@gmail.com", "com.google"),
        )
        val manager = GoogleAuthManager(
            activity = activity,
            onEmailReady = {},
            getGoogleAccounts = { invoked = true; accounts },
        )
        // checkExistingAuthorization requires Google Identity — catch the runtime error.
        try { manager.checkExisting(context) } catch (_: Exception) {}
        assertTrue("getGoogleAccounts lambda must be invoked", invoked)
    }
}
