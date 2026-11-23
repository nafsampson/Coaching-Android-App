// Converts authentication failures into readable messages.

package com.formup.app.ui.auth

import androidx.annotation.StringRes
import androidx.credentials.exceptions.NoCredentialException
import com.formup.app.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import java.io.IOException

@StringRes
internal fun authErrorMessage(error: Exception): Int = when (error) {
    is NoCredentialException -> R.string.auth_google_setup
    is FirebaseNetworkException, is IOException -> R.string.auth_network
    is FirebaseAuthException -> when (error.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> R.string.auth_email_exists
        "ERROR_INVALID_EMAIL" -> R.string.auth_invalid_email
        "ERROR_WEAK_PASSWORD" -> R.string.mod_password_length
        "ERROR_OPERATION_NOT_ALLOWED" -> R.string.auth_provider_disabled
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> R.string.auth_credentials
        "ERROR_TOO_MANY_REQUESTS" -> R.string.auth_try_later
        else -> R.string.msg_sign_in_failed
    }
    else -> R.string.msg_sign_in_failed
}
