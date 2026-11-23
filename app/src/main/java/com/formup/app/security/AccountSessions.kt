// Checks that requests still belong to the current sign-in session.

package com.formup.app.security

import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SessionLease(val uid: String, val id: String = UUID.randomUUID().toString()) {
    fun check() {
        if (AccountSessions.current.value != this || FirebaseAuth.getInstance().currentUser?.uid != uid) {
            throw CancellationException("The account session changed")
        }
    }
}

object AccountSessions {
    private val auth = FirebaseAuth.getInstance()
    private val state = MutableStateFlow(auth.currentUser?.uid?.let { SessionLease(it) })
    val current: StateFlow<SessionLease?> = state
    private val listener = FirebaseAuth.AuthStateListener { firebase ->
        val uid = firebase.currentUser?.uid
        if (state.value?.uid != uid) state.value = uid?.let { SessionLease(it) }
    }
    init { auth.addAuthStateListener(listener) }
    fun requireSession(): SessionLease {
        val uid = auth.currentUser?.uid ?: throw CancellationException("Sign in to continue")
        if (state.value?.uid != uid) state.value = SessionLease(uid)
        return state.value!!.also { it.check() }
    }

    fun signOut() {

        // Invalidate active requests before Firebase completes sign-out.
        state.value = null
        auth.signOut()
    }
}

inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
// Let sign-out and cancelled work stop immediately.
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: Exception) {
    Result.failure(error)
}
