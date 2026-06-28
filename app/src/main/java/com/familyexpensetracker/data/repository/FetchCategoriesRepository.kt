package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.local.LocalCategoriesDataSource
import com.familyexpensetracker.data.remote.FetchCategoriesDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FetchCategoriesRepository(
    private val remoteDataSource: FetchCategoriesDataSource,
    private val localDataSource: LocalCategoriesDataSource,
) {
    
    companion object {
        private var cachedCategories: Map<String, List<String>>? = null
        private val mutex = Mutex()
    }

    suspend fun fetch(): Map<String, List<String>> {
        // 1. Check RAM cache
        cachedCategories?.let { return it }
        
        return mutex.withLock {
            // 2. Double check RAM cache
            cachedCategories?.let { return@withLock it }
            
            // 3. Check Persistence (ROM)
            val localCategories = localDataSource.getCategories()
            if (localCategories != null) {
                cachedCategories = localCategories
                return@withLock localCategories
            }
            
            // 4. Fetch from remote and save
            val remoteCategories = remoteDataSource.fetch()
            localDataSource.saveCategories(remoteCategories)
            cachedCategories = remoteCategories
            remoteCategories
        }
    }
}
