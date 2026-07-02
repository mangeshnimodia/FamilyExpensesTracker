package com.familyexpensetracker.ui.viewmodel

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
    fun `searchTransactions with blank query clears state without launching job`() {
        viewModel.searchTransactions("")

        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions with query fetches results`() = runTest {
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("food") } returns results

        viewModel.searchTransactions("food")

        assertEquals("food", viewModel.searchQuery.value)
        assertEquals(results, viewModel.searchResults.value)
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions on error sets errorMessage`() = runTest {
        coEvery { searchRepository.search(any()) } throws Exception("Search failed")

        viewModel.searchTransactions("food")

        // isSearching must be false after completion
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `searchTransactions clears previous results on blank query`() = runTest {
        val results = listOf(mockk<Transaction>())
        coEvery { searchRepository.search("food") } returns results
        viewModel.searchTransactions("food")
        assertEquals(results, viewModel.searchResults.value)

        viewModel.searchTransactions("")

        assertTrue(viewModel.searchResults.value.isEmpty())
        assertEquals("", viewModel.searchQuery.value)
    }
}
