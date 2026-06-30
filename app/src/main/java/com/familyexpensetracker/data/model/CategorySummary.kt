package com.familyexpensetracker.data.model

data class CategorySummary(
    val category: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val subcategories: List<SubcategoryGroup> = emptyList()
)
