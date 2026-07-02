package com.familyexpensetracker.data.remote

import com.familyexpensetracker.data.model.Transaction
import com.familyexpensetracker.utils.AppConstants

class TransactionRowMapper {
    fun mapRow(row: List<Any>): Transaction = Transaction(
        txnId         = row.getOrNull(AppConstants.COL_TXN_ID)?.toString() ?: "",
        date          = row.getOrNull(AppConstants.COL_DATE)?.toString() ?: "",
        amount        = row.getOrNull(AppConstants.COL_AMOUNT)?.toString()?.toDoubleOrNull() ?: 0.0,
        category      = row.getOrNull(AppConstants.COL_CATEGORY)?.toString() ?: "",
        subcategory   = row.getOrNull(AppConstants.COL_SUBCATEGORY)?.toString() ?: "",
        paymentMethod = row.getOrNull(AppConstants.COL_PAYMENT_METHOD)?.toString() ?: "",
        description   = row.getOrNull(AppConstants.COL_DESCRIPTION)?.toString() ?: "",
        account       = row.getOrNull(AppConstants.COL_ACCOUNT)?.toString() ?: "",
        transferId    = row.getOrNull(AppConstants.COL_TRANSFER_ID)?.toString(),
    )
}
