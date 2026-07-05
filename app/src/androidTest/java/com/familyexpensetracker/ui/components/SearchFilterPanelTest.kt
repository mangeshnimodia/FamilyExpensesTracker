package com.familyexpensetracker.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.familyexpensetracker.data.model.SearchFilter
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchFilterPanelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun defaultFilter() = SearchFilter()
    private val accounts = listOf("Passbook", "Wallet")
    private val categories = listOf("Food", "Transport")
    private val subcategories = listOf("Groceries", "Fuel")
    private val paymentMethods = listOf("Cash", "UPI")

    private fun setContent(
        filter: SearchFilter = defaultFilter(),
        onFilterChange: (SearchFilter) -> Unit = {},
    ) {
        composeTestRule.setContent {
            SearchFilterPanel(
                filter = filter,
                accounts = accounts,
                categories = categories,
                subcategories = subcategories,
                paymentMethods = paymentMethods,
                onFilterChange = onFilterChange,
            )
        }
    }

    @Test
    fun searchFilterPanel_displaysAccountLabel() {
        setContent()
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysCategoryLabel() {
        setContent()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysSubcategoryLabel() {
        setContent()
        composeTestRule.onNodeWithText("Subcategory").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysPaymentMethodLabel() {
        setContent()
        composeTestRule.onNodeWithText("Payment Method").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysMinAmountLabel() {
        setContent()
        composeTestRule.onNodeWithText("Min Amount").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysMaxAmountLabel() {
        setContent()
        composeTestRule.onNodeWithText("Max Amount").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysExactMatchText() {
        setContent()
        composeTestRule.onNodeWithText("Exact match").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_exactMatchCheckboxUncheckedByDefault() {
        setContent()
        composeTestRule.onNodeWithText("Exact match").assertIsDisplayed()
        composeTestRule.onNode(hasSetTextAction().not().and(isToggleable())).assertIsOff()
    }

    @Test
    fun searchFilterPanel_checkingExactMatchCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNode(isToggleable()).performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertTrue(slot.captured.exactMatch)
    }

    @Test
    fun searchFilterPanel_uncheckingExactMatchCallsOnFilterChangeWithFalse() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(filter = SearchFilter(exactMatch = true), onFilterChange = onFilterChange)

        composeTestRule.onNode(isToggleable()).performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertFalse(slot.captured.exactMatch)
    }

    @Test
    fun searchFilterPanel_minAmountInputCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Min Amount").performTextInput("100")

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals(100.0, slot.captured.minAmount)
    }

    @Test
    fun searchFilterPanel_maxAmountInputCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Max Amount").performTextInput("500")

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals(500.0, slot.captured.maxAmount)
    }

    @Test
    fun searchFilterPanel_invalidAmountInputResultsInNullMinAmount() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Min Amount").performTextInput("abc")

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertNull(slot.captured.minAmount)
    }

    @Test
    fun searchFilterPanel_displaysSelectedAccountValue() {
        setContent(filter = SearchFilter(account = "Passbook"))
        composeTestRule.onNodeWithText("Passbook").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysSelectedCategoryValue() {
        setContent(filter = SearchFilter(category = "Food"))
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysSelectedPaymentMethodValue() {
        setContent(filter = SearchFilter(paymentMethod = "Cash"))
        composeTestRule.onNodeWithText("Cash").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_displaysMinAndMaxAmountValues() {
        setContent(filter = SearchFilter(minAmount = 50.0, maxAmount = 200.0))
        composeTestRule.onNodeWithText("50.0").assertIsDisplayed()
        composeTestRule.onNodeWithText("200.0").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_accountDropdownShowsOptionsWhenExpanded() {
        composeTestRule.setContent {
            SearchFilterPanel(
                filter = defaultFilter(),
                accounts = accounts,
                categories = categories,
                subcategories = subcategories,
                paymentMethods = paymentMethods,
                onFilterChange = {},
            )
        }

        composeTestRule.onNodeWithText("Account").performClick()

        composeTestRule.onNodeWithText("Passbook").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wallet").assertIsDisplayed()
    }

    @Test
    fun searchFilterPanel_selectingAccountOptionCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Account").performClick()
        composeTestRule.onNodeWithText("Passbook").performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals("Passbook", slot.captured.account)
    }

    @Test
    fun searchFilterPanel_selectingCategoryOptionCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Category").performClick()
        composeTestRule.onNodeWithText("Food").performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals("Food", slot.captured.category)
    }

    @Test
    fun searchFilterPanel_selectingSubcategoryOptionCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Subcategory").performClick()
        composeTestRule.onNodeWithText("Groceries").performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals("Groceries", slot.captured.subcategory)
    }

    @Test
    fun searchFilterPanel_selectingPaymentMethodOptionCallsOnFilterChange() {
        val onFilterChange: (SearchFilter) -> Unit = mockk(relaxed = true)
        setContent(onFilterChange = onFilterChange)

        composeTestRule.onNodeWithText("Payment Method").performClick()
        composeTestRule.onNodeWithText("UPI").performClick()

        val slot = slot<SearchFilter>()
        verify { onFilterChange(capture(slot)) }
        assertEquals("UPI", slot.captured.paymentMethod)
    }

    @Test
    fun searchFilterPanel_searchButton_callsOnSearch() {
        val onSearch: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            SearchFilterPanel(defaultFilter(), accounts, categories, subcategories, paymentMethods, {}, onSearch = onSearch)
        }
        composeTestRule.onNodeWithText("Search").performClick()
        verify { onSearch() }
    }

    @Test
    fun searchFilterPanel_clearButton_callsOnClear() {
        val onClear: () -> Unit = mockk(relaxed = true)
        composeTestRule.setContent {
            SearchFilterPanel(defaultFilter(), accounts, categories, subcategories, paymentMethods, {}, onClear = onClear)
        }
        composeTestRule.onNodeWithText("Clear").performClick()
        verify { onClear() }
    }
}
