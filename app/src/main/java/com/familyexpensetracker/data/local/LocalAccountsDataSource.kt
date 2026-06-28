package com.familyexpensetracker.data.local

import android.content.Context
import com.familyexpensetracker.utils.AppConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class LocalAccountsDataSource(context: Context) {
    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAccounts(): List<String>? {
        val json = prefs.getString(AppConstants.KEY_ACCOUNTS_LIST, null) ?: return null
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun saveAccounts(accounts: List<String>) {
        val json = gson.toJson(accounts)
        prefs.edit { putString(AppConstants.KEY_ACCOUNTS_LIST, json) }
    }
}
