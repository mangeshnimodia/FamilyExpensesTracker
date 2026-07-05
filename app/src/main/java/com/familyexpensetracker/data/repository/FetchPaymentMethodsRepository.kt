package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalPaymentMethodsDataSource
import com.familyexpensetracker.data.remote.FetchPaymentMethodsDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FetchPaymentMethodsRepository(
    private val remoteDataSource: FetchPaymentMethodsDataSource,
    private val localDataSource: LocalPaymentMethodsDataSource,
) {
    private var cachedPaymentMethods: List<String>? = null
    private val mutex = Mutex()

    suspend fun fetch(): List<String> {
        cachedPaymentMethods?.let { return it }

        return mutex.withLock {
            cachedPaymentMethods?.let { return@withLock it }

            val local = localDataSource.getPaymentMethods()
            if (local != null) {
                cachedPaymentMethods = local
                return@withLock local
            }

            val remote = remoteDataSource.fetch()
            localDataSource.savePaymentMethods(remote)
            cachedPaymentMethods = remote
            remote
        }
    }
}
