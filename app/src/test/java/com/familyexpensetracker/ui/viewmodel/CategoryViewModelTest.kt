package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.repository.FetchCategoriesRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModelTest {

    private val expenseCategoriesRepository = mockk<FetchCategoriesRepository>()
    private val incomeCategoriesRepository = mockk<FetchCategoriesRepository>()

    private lateinit var viewModel: CategoryViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CategoryViewModel(expenseCategoriesRepository, incomeCategoriesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `expenseCategories defaults to empty map`() {
        assertTrue(viewModel.expenseCategories.value.isEmpty())
    }

    @Test
    fun `incomeCategories defaults to empty map`() {
        assertTrue(viewModel.incomeCategories.value.isEmpty())
    }

    @Test
    fun `loadCategories updates both expense and income categories`() = runTest {
        val expenses = mapOf("Food" to listOf("Groceries"))
        val income = mapOf("Job" to listOf("Salary"))
        coEvery { expenseCategoriesRepository.fetch() } returns expenses
        coEvery { incomeCategoriesRepository.fetch() } returns income

        viewModel.loadCategories()

        assertEquals(expenses, viewModel.expenseCategories.value)
        assertEquals(income, viewModel.incomeCategories.value)
    }

    @Test
    fun `loadCategories on error leaves categories unchanged`() = runTest {
        coEvery { expenseCategoriesRepository.fetch() } throws Exception("Network error")
        coEvery { incomeCategoriesRepository.fetch() } returns emptyMap()

        viewModel.loadCategories()

        assertTrue(viewModel.expenseCategories.value.isEmpty())
    }

    @Test
    fun `loadCategories is idempotent when called twice`() = runTest {
        val expenses = mapOf("Food" to listOf("Groceries"))
        val income = mapOf("Job" to listOf("Salary"))
        coEvery { expenseCategoriesRepository.fetch() } returns expenses
        coEvery { incomeCategoriesRepository.fetch() } returns income

        viewModel.loadCategories()
        viewModel.loadCategories()

        assertEquals(expenses, viewModel.expenseCategories.value)
        assertEquals(income, viewModel.incomeCategories.value)
    }
}
