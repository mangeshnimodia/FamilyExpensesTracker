package com.familyexpensetracker.data.repository

import com.familyexpensetracker.data.remote.FetchTransactionsDataSource
import java.util.*

class FetchTransactionsRepository(private val dataSource: FetchTransactionsDataSource) {
    suspend fun fetch(startDate: Date? = null) = dataSource.fetch(startDate)
}
