package com.familyexpensetracker.utils

object AppConstants {
    const val SPREADSHEET_ID = "17Vf1XbIATnDRslj5BoNJFlDyq7SFINnyiAhX-Sx1FWU"
    
    const val RANGE_TRANSACTIONS = "Transactions!A:I"
    const val RANGE_TXN_IDS = "Transactions!A:A"
    const val SHEET_NAME_TRANSACTIONS = "Transactions"
    const val RANGE_CATEGORIES = "admin!A:B"
    const val RANGE_ACCOUNTS = "admin!D:D"
    
    const val APPLICATION_NAME = "Family Expense Tracker"
    
    val SCOPES = listOf(
        "https://www.googleapis.com/auth/spreadsheets",
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile",
    )
    
    const val DEFAULT_CATEGORY = "Daily Living"
    const val DEFAULT_SUBCATEGORY = "Groceries"
    const val DEFAULT_PAYMENT_METHOD = "Cash"
    const val DEFAULT_ACCOUNT = "Passbook"
    
    const val PREFS_NAME = "app_prefs"
    const val KEY_CATEGORIES_MAP = "categories_map"
    const val KEY_ACCOUNTS_LIST = "accounts_list"
}
