package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalPaymentMethodsDataSource
import com.familyexpensetracker.data.remote.FetchPaymentMethodsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FetchPaymentMethodsRepositoryTest {

    private val remoteDataSource = mockk<FetchPaymentMethodsDataSource>()
    private val localDataSource = mockk<LocalPaymentMethodsDataSource>()
    private lateinit var repository: FetchPaymentMethodsRepository

    @Before
    fun setUp() {
        repository = FetchPaymentMethodsRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun `fetch returns local payment methods if available`() = runBlocking {
        val methods = listOf("Cash", "UPI")
        every { localDataSource.getPaymentMethods() } returns methods

        val result = repository.fetch()

        assertEquals(methods, result)
        coVerify(exactly = 0) { remoteDataSource.fetch() }
    }

    @Test
    fun `fetch returns remote payment methods if local not available`() = runBlocking<Unit> {
        val methods = listOf("Cash", "Card")
        every { localDataSource.getPaymentMethods() } returns null
        coEvery { remoteDataSource.fetch() } returns methods
        every { localDataSource.savePaymentMethods(any()) } returns Unit

        val result = repository.fetch()

        assertEquals(methods, result)
        coVerify { remoteDataSource.fetch() }
        verify { localDataSource.savePaymentMethods(methods) }
    }
}
