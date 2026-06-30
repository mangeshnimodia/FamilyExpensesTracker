package com.familyexpensetracker.data.model

data class SubcategoryGroup(
    val subcategory: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val transactions: List<Transaction> = emptyList()
)
