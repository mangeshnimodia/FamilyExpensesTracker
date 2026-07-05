package com.familyexpensetracker.ui.viewmodel

import com.familyexpensetracker.data.repository.FetchPaymentMethodsRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodViewModelTest {

    private val paymentMethodsRepository = mockk<FetchPaymentMethodsRepository>()

    private lateinit var viewModel: PaymentMethodViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentMethodViewModel(paymentMethodsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `paymentMethods defaults to empty list`() {
        assertTrue(viewModel.paymentMethods.value.isEmpty())
    }

    @Test
    fun `loadPaymentMethods updates paymentMethods state`() = runTest {
        val methods = listOf("Cash", "UPI", "Card")
        coEvery { paymentMethodsRepository.fetch() } returns methods

        viewModel.loadPaymentMethods()

        assertEquals(methods, viewModel.paymentMethods.value)
    }

    @Test
    fun `loadPaymentMethods on error leaves paymentMethods unchanged`() = runTest {
        coEvery { paymentMethodsRepository.fetch() } throws Exception("Network error")

        viewModel.loadPaymentMethods()

        assertTrue(viewModel.paymentMethods.value.isEmpty())
    }

    @Test
    fun `loadPaymentMethods is idempotent when called twice`() = runTest {
        val methods = listOf("Cash", "UPI")
        coEvery { paymentMethodsRepository.fetch() } returns methods

        viewModel.loadPaymentMethods()
        viewModel.loadPaymentMethods()

        assertEquals(methods, viewModel.paymentMethods.value)
    }

    @Test
    @org.junit.Ignore("Un-ignore in Step 3")
    // Un-ignore in Step 3: assert viewModel.errorMessage.value is non-null after datasource throws
    fun loadPaymentMethods_setsErrorMessage_onDatasourceException() = runTest {
        coEvery { paymentMethodsRepository.fetch() } throws Exception("Network failure")
        viewModel.loadPaymentMethods()
    }
}
