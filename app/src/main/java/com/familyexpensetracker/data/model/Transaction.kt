package com.familyexpensetracker.data.model

data class Transaction(
    val txnId: String = "",
    val date: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val subcategory: String = "",
    val paymentMethod: String = "",
    val description: String = "",
    val account: String = "",
    val transferId: String? = null
)
