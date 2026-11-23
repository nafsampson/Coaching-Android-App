// Signs coaches in and checks whether team setup is complete.

package com.formup.app.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.formup.app.R
import com.formup.app.data.FormUpApi
import com.formup.app.security.AccountSessions
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

private suspend fun finishSignIn(): Boolean {
    val snapshot = FormUpApi(session = AccountSessions.requireSession()).loadSeason()
    return snapshot.team.name.isNotBlank()
}

suspend fun emailSignIn(email: String, password: String): Boolean {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
    return finishSignIn()
}

suspend fun emailSignUp(name: String, email: String, password: String): Boolean {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
    val api = FormUpApi(session = AccountSessions.requireSession())
    api.loadSeason()
    api.updateCoach(displayName = name)
    return false
}

suspend fun googleSignIn(context: Context): Boolean {

    val webClientId = context.getString(R.string.default_web_client_id)

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
        .build()

    val credential = CredentialManager.create(context).getCredential(context, request).credential

    require(
        credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) { "Unexpected credential type" }

    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
    FirebaseAuth.getInstance()
        .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    return finishSignIn()
}
