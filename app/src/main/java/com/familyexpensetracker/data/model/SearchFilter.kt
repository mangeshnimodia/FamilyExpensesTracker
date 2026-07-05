package com.familyexpensetracker.data.model

data class SearchFilter(
    val account: String = "",
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val category: String = "",
    val subcategory: String = "",
    val paymentMethod: String = "",
    val exactMatch: Boolean = false,
) {
    fun isEmpty(): Boolean =
        account.isBlank() &&
            minAmount == null &&
            maxAmount == null &&
            category.isBlank() &&
            subcategory.isBlank() &&
            paymentMethod.isBlank() &&
            !exactMatch
}
