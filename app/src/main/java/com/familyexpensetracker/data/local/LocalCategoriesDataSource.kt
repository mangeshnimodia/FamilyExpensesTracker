package com.familyexpensetracker.data.local

import android.content.Context
import com.familyexpensetracker.utils.AppConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class LocalCategoriesDataSource(
    context: Context,
    private val key: String,
) {
    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCategories(): Map<String, List<String>>? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun saveCategories(categories: Map<String, List<String>>) {
        val json = gson.toJson(categories)
        prefs.edit { putString(key, json) }
    }
}
