package com.familyexpensetracker.data.local

import android.content.Context
import androidx.core.content.edit
import com.familyexpensetracker.utils.AppConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalPaymentMethodsDataSource(context: Context) {
    private val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getPaymentMethods(): List<String>? {
        val json = prefs.getString(AppConstants.KEY_PAYMENT_METHODS_LIST, null) ?: return null
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            null
        }
    }

    fun savePaymentMethods(paymentMethods: List<String>) {
        val json = gson.toJson(paymentMethods)
        prefs.edit { putString(AppConstants.KEY_PAYMENT_METHODS_LIST, json) }
    }
}
