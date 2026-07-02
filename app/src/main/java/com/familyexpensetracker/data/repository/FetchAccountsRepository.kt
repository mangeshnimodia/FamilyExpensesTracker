package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalAccountsDataSource
import com.familyexpensetracker.data.remote.FetchAccountsDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FetchAccountsRepository(
    private val remoteDataSource: FetchAccountsDataSource,
    private val localDataSource: LocalAccountsDataSource,
) {
    private var cachedAccounts: List<String>? = null
    private val mutex = Mutex()

    suspend fun fetch(): List<String> {
        cachedAccounts?.let { return it }

        return mutex.withLock {
            cachedAccounts?.let { return@withLock it }

            val localAccounts = localDataSource.getAccounts()
            if (localAccounts != null) {
                cachedAccounts = localAccounts
                return@withLock localAccounts
            }

            val remoteAccounts = remoteDataSource.fetch()
            localDataSource.saveAccounts(remoteAccounts)
            cachedAccounts = remoteAccounts
            remoteAccounts
        }
    }
}
