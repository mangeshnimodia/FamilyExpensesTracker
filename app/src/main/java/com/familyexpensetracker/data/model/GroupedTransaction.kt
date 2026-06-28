package com.familyexpensetracker.data.model

data class AccountGroup(
    val name: String,
    val total: Double,
    val categories: List<CategoryGroup>
)

data class CategoryGroup(
    val name: String,
    val total: Double,
    val transactions: List<Transaction>
)
