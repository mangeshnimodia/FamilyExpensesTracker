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

class LocalCategoriesDataSourceTest {

    private val context = mockk<Context>()
    private val prefs = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()
    private val testKey = "test_categories_key"
    private lateinit var dataSource: LocalCategoriesDataSource

    @Before
    fun setUp() {
        every { context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE) } returns prefs
        dataSource = LocalCategoriesDataSource(context, testKey)
    }

    @Test
    fun `getCategories returns map when json exists`() {
        val json = "{\"Food\":[\"Groceries\"]}"
        every { prefs.getString(testKey, null) } returns json

        val result = dataSource.getCategories()

        assertEquals(mapOf("Food" to listOf("Groceries")), result)
    }

    @Test
    fun `getCategories returns null when json not exists`() {
        every { prefs.getString(testKey, null) } returns null

        val result = dataSource.getCategories()

        assertNull(result)
    }

    @Test
    fun `saveCategories saves json to prefs`() {
        val categories = mapOf("Food" to listOf("Groceries"))
        every { prefs.edit() } returns editor
        every { editor.putString(testKey, any()) } returns editor
        every { editor.apply() } returns Unit

        dataSource.saveCategories(categories)

        verify {
            editor.putString(testKey, "{\"Food\":[\"Groceries\"]}")
            editor.apply()
        }
    }
}
