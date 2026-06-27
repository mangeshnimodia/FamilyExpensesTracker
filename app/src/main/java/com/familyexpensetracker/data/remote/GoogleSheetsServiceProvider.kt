package com.familyexpensetracker.data.remote

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets

class GoogleSheetsServiceProvider(private val context: Context) {
    
    fun getSheetsService(accountName: String): Sheets {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf("https://www.googleapis.com/auth/spreadsheets")
        ).setSelectedAccountName(accountName)

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Family Expense Tracker").build()
    }
}
