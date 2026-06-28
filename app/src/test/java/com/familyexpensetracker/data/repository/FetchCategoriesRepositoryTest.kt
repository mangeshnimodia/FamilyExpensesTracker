package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalCategoriesDataSource
import com.familyexpensetracker.data.remote.FetchCategoriesDataSource
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchCategoriesRepositoryTest {

    private val remoteDataSource = mockk<FetchCategoriesDataSource>()
    private val localDataSource = mockk<LocalCategoriesDataSource>()
    private lateinit var repository: FetchCategoriesRepository

    @Before
    fun setUp() {
        repository = FetchCategoriesRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun `fetch returns local categories if available`() = runBlocking {
        val categories = mapOf("Food" to listOf("Groceries"))
        every { localDataSource.getCategories() } returns categories

        val result = repository.fetch()

        assertEquals(categories, result)
        coVerify(exactly = 0) { remoteDataSource.fetch() }
    }

    @Test
    fun `fetch returns remote categories if local not available`() = runBlocking {
        val categories = mapOf("Rent" to emptyList<String>())
        every { localDataSource.getCategories() } returns null
        coEvery { remoteDataSource.fetch() } returns categories
        every { localDataSource.saveCategories(any()) } returns Unit

        val result = repository.fetch()

        assertEquals(categories, result)
        coVerify { remoteDataSource.fetch() }
        verify { localDataSource.saveCategories(categories) }
    }
}
