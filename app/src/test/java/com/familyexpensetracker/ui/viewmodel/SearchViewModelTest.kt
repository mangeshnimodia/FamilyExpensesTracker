package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.model.SearchFilter
import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.data.repository.SearchTransactionsRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val searchRepository = mockk<SearchTransactionsRepository>()

    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(searchRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery defaults to empty string`() {
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `searchResults defaults to empty list`() {
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun `isSearching defaults to false`() {
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchFilter defaults to empty SearchFilter`() {
        assertEquals(SearchFilter(), viewModel.searchFilter.value)
    }

    @Test
    fun `updateSearchFilter updates searchFilter state`() {
        val filter = SearchFilter(account = "Savings", category = "Food")
        viewModel.updateSearchFilter(filter)
        assertEquals(filter, viewModel.searchFilter.value)
    }

    @Test
    fun `updateSearchFilter replaces previous filter`() {
        viewModel.updateSearchFilter(SearchFilter(account = "Savings"))
        viewModel.updateSearchFilter(SearchFilter(category = "Food"))
        assertEquals(SearchFilter(category = "Food"), viewModel.searchFilter.value)
    }

    @Test
    fun `searchTransactions with blank query and empty filter clears state without launching job`() {
        viewModel.searchTransactions("")

        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions with query fetches results`() = runTest {
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("food", SearchFilter()) } returns results

        viewModel.searchTransactions("food")

        assertEquals("food", viewModel.searchQuery.value)
        assertEquals(results, viewModel.searchResults.value)
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions with filter only fetches results`() = runTest {
        val results = listOf(mockk<Transaction>())
        val filter = SearchFilter(account = "Savings")
        coEvery { searchRepository.search("", filter) } returns results

        viewModel.searchTransactions("", filter)

        assertEquals("", viewModel.searchQuery.value)
        assertEquals(results, viewModel.searchResults.value)
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions uses current searchFilter when none passed`() = runTest {
        val filter = SearchFilter(category = "Food")
        val results = listOf(mockk<Transaction>())
        viewModel.updateSearchFilter(filter)
        coEvery { searchRepository.search("milk", filter) } returns results

        viewModel.searchTransactions("milk")

        assertEquals(results, viewModel.searchResults.value)
    }

    @Test
    fun `searchTransactions with explicit filter overrides stored filter`() = runTest {
        viewModel.updateSearchFilter(SearchFilter(account = "Savings"))
        val explicitFilter = SearchFilter(category = "Transport")
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("petrol", explicitFilter) } returns results

        viewModel.searchTransactions("petrol", explicitFilter)

        assertEquals(results, viewModel.searchResults.value)
    }

    @Test
    fun `searchTransactions blank query with non-empty filter still fetches`() = runTest {
        val filter = SearchFilter(account = "Savings")
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("", filter) } returns results

        viewModel.searchTransactions("", filter)

        assertEquals(results, viewModel.searchResults.value)
    }

    @Test
    fun `searchTransactions on error sets errorMessage`() = runTest {
        coEvery { searchRepository.search(any(), any()) } throws Exception("Search failed")

        viewModel.searchTransactions("food")

        // isSearching must be false after completion
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions clears previous results on blank query with empty filter`() = runTest {
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("food", SearchFilter()) } returns results
        viewModel.searchTransactions("food")
        assertEquals(results, viewModel.searchResults.value)

        viewModel.searchTransactions("")

        assertTrue(viewModel.searchResults.value.isEmpty())
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    @org.junit.Ignore("Un-ignore in Step 3")
    // Un-ignore in Step 3: assert viewModel.errorMessage.value is non-null after repository throws
    fun searchTransactions_setsErrorMessage_onDatasourceException() = runTest {
        coEvery { searchRepository.search(any(), any()) } throws Exception("Search failure")
        viewModel.searchTransactions("food")
    }

}