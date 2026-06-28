package com.familyexpensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import com.familyexpensetracker.utils.AppConstants
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalAccountsDataSourceTest {

    private val context = mockk<Context>()
    private val prefs = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()
    private lateinit var dataSource: LocalAccountsDataSource

    @Before
    fun setUp() {
        every { context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE) } returns prefs
        dataSource = LocalAccountsDataSource(context)
    }

    @Test
    fun `getAccounts returns list when json exists`() {
        val json = "[\"Savings\",\"Cash\"]"
        every { prefs.getString(AppConstants.KEY_ACCOUNTS_LIST, null) } returns json

        val result = dataSource.getAccounts()

        assertEquals(listOf("Savings", "Cash"), result)
    }

    @Test
    fun `getAccounts returns null when json not exists`() {
        every { prefs.getString(AppConstants.KEY_ACCOUNTS_LIST, null) } returns null

        val result = dataSource.getAccounts()

        assertNull(result)
    }

    @Test
    fun `saveAccounts saves json to prefs`() {
        val accounts = listOf("Savings", "Cash")
        every { prefs.edit() } returns editor
        every { editor.putString(AppConstants.KEY_ACCOUNTS_LIST, any()) } returns editor
        every { editor.apply() } returns Unit

        dataSource.saveAccounts(accounts)

        verify {
            editor.putString(AppConstants.KEY_ACCOUNTS_LIST, "[\"Savings\",\"Cash\"]")
            editor.apply()
        }
    }
}
