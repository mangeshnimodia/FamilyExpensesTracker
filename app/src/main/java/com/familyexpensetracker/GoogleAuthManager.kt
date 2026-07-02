package com.familyexpensetracker

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.familyexpensetracker.utils.AppConstants
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

class GoogleAuthManager(
    private val activity: ComponentActivity,
    private val onEmailReady: (String) -> Unit,
    private val getGoogleAccounts: (Context) -> Array<Account> = { ctx ->
        AccountManager.get(ctx).getAccountsByType(AppConstants.GOOGLE_ACCOUNT_TYPE)
    },
) {
    private lateinit var accountPickerLauncher: ActivityResultLauncher<android.content.Intent>
    private lateinit var authorizeLauncher: ActivityResultLauncher<IntentSenderRequest>

    fun register() {
        accountPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                email?.let { authorizeAndInitialize(it) }
            }
        }

        authorizeLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val authResult = Identity.getAuthorizationClient(activity)
                        .getAuthorizationResultFromIntent(result.data)
                    val email = authResult.toGoogleSignInAccount()?.email
                    if (email != null) {
                        onEmailReady(email)
                    } else {
                        val accounts = getGoogleAccounts(activity)
                        if (accounts.isNotEmpty()) {
                            onEmailReady(accounts[0].name)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun checkExisting(context: Context) {
        val accounts = getGoogleAccounts(context)
        if (accounts.isNotEmpty()) {
            checkExistingAuthorization(accounts[0].name)
        }
    }

    fun startSignIn() {
        val intent = AccountManager.newChooseAccountIntent(
            null, null, arrayOf(AppConstants.GOOGLE_ACCOUNT_TYPE),
            null, null, null, null,
        )
        accountPickerLauncher.launch(intent)
    }

    private fun checkExistingAuthorization(email: String) {
        val requestedScopes = AppConstants.SCOPES.map { Scope(it) }
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .setAccount(Account(email, AppConstants.GOOGLE_ACCOUNT_TYPE))
            .build()

        Identity.getAuthorizationClient(activity)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (!result.hasResolution()) {
                    onEmailReady(email)
                }
            }
            .addOnFailureListener { }
    }

    private fun authorizeAndInitialize(email: String) {
        val requestedScopes = AppConstants.SCOPES.map { Scope(it) }
        val authorizationRequest = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .setAccount(Account(email, AppConstants.GOOGLE_ACCOUNT_TYPE))
            .build()

        Identity.getAuthorizationClient(activity)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    authorizeLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build(),
                    )
                } else {
                    onEmailReady(email)
                }
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }
}
