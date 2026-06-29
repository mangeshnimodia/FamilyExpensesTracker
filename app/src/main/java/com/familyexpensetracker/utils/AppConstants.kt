package com.familyexpensetracker.utils

object AppConstants {
    const val SPREADSHEET_ID = "17Vf1XbIATnDRslj5BoNJFlDyq7SFINnyiAhX-Sx1FWU"
    
    const val RANGE_TRANSACTIONS = "Transactions!A:I"
    const val RANGE_TXN_IDS = "Transactions!A:A"
    const val SHEET_NAME_TRANSACTIONS = "Transactions"
    const val RANGE_EXPENSE_CATEGORIES = "admin!A:B"
    const val RANGE_ACCOUNTS = "admin!D:D"
    const val RANGE_INCOME_CATEGORIES = "admin!F:G"
    
    const val APPLICATION_NAME = "Family Expense Tracker"
    
    val SCOPES = listOf(
        "https://www.googleapis.com/auth/spreadsheets",
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile",
    )
    
    const val DEFAULT_CATEGORY = "Daily Living"
    const val DEFAULT_SUBCATEGORY = "Groceries"
    const val DEFAULT_INCOME_CATEGORY = "Income"
    const val DEFAULT_INCOME_SUBCATEGORY = "Salary"
    const val CATEGORY_ACCOUNT_TRANSFER = "Account Transfer"
    const val SUBCATEGORY_ACCOUNT_TRANSFER = "Account Transfer"
    const val DEFAULT_PAYMENT_METHOD = "Cash"
    const val DEFAULT_ACCOUNT = "Passbook"
    
    const val PREFS_NAME = "app_prefs"
    const val KEY_EXPENSE_CATEGORIES_MAP = "expense_categories_map"
    const val KEY_INCOME_CATEGORIES_MAP = "income_categories_map"
    const val KEY_ACCOUNTS_LIST = "accounts_list"

    const val DATE_FORMAT_DB = "yyyy/MM/dd"
    const val DATE_FORMAT_UI = "EEE, dd MMM yyyy"

    // Transaction Column Indexes
    const val COL_TXN_ID = 0
    const val COL_DATE = 1
    const val COL_AMOUNT = 2
    const val COL_CATEGORY = 3
    const val COL_SUBCATEGORY = 4
    const val COL_PAYMENT_METHOD = 5
    const val COL_DESCRIPTION = 6
    const val COL_ACCOUNT = 7
    const val COL_TRANSFER_ID = 8

    // Admin Column Indexes (Relative to their specific ranges)
    const val COL_ADMIN_CATEGORY = 0
    const val COL_ADMIN_SUBCATEGORY = 1
    const val COL_ADMIN_ACCOUNT = 0
}
