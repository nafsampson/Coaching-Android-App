// Handles sign-in, team setup, biometric locking and the main app flow.

package com.formup.app.ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.formup.app.R
import com.formup.app.data.FormUpApi
import com.formup.app.security.AccountSessions
import com.formup.app.security.BiometricAuth
import com.formup.app.security.BiometricLockScreen
import com.formup.app.security.BiometricPreferences
import com.formup.app.ui.FormUpViewModel
import com.formup.app.ui.auth.CreateTeamScreen
import com.formup.app.ui.auth.LoginScreen
import com.formup.app.ui.auth.NewAccount
import com.formup.app.ui.auth.SignUpScreen
import com.formup.app.ui.auth.emailSignIn
import com.formup.app.ui.auth.emailSignUp
import com.formup.app.ui.auth.googleSignIn
import com.formup.app.ui.home.components.HomeTab
import com.formup.app.ui.navigation.Destination
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun FormUpApp(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    notificationIntent: Intent? = null,
    onNotificationIntentHandled: () -> Unit = {}
) {
    val session by AccountSessions.current.collectAsState()
    val viewModel: FormUpViewModel = viewModel(key = "coach-${session?.id ?: "signed-out"}")
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    var signedIn by remember { mutableStateOf(auth.currentUser != null) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var isAppInForeground by remember {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState
                .isAtLeast(Lifecycle.State.STARTED)
        )
    }

    var isBiometricPromptActive by remember {
        mutableStateOf(false)
    }

    var passwordFallbackPending by remember {
        mutableStateOf(false)
    }

    var biometricBypassForSession by remember {
        mutableStateOf(false)
    }

    var biometricEnabled by remember(auth.currentUser?.uid) {
        mutableStateOf(
            auth.currentUser?.uid?.let { userId ->
                BiometricPreferences.isEnabled(context, userId)
            } ?: false
        )
    }

    var isAppUnlocked by remember {
        mutableStateOf(
            auth.currentUser?.uid?.let { userId ->
                !BiometricPreferences.isEnabled(context, userId)
            } ?: true
        )
    }

    var biometricLockMessage by remember {
        mutableStateOf(context.getString(R.string.loc_coach_protected))
    }

    var authenticating by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showSignUp by remember { mutableStateOf(false) }
    var needsTeam by remember(session?.id) { mutableStateOf(false) }
    var resolvingAccount by remember(session?.id) { mutableStateOf(session != null) }
    var accountLoadFailed by remember(session?.id) { mutableStateOf(false) }
    var accountRetry by remember { mutableStateOf(0) }
    LaunchedEffect(session?.id, accountRetry, authenticating) {
        val owner = session ?: return@LaunchedEffect
        if (authenticating) return@LaunchedEffect
        try {
            resolvingAccount = true
            val snapshot = accountSnapshot(context, owner)
            needsTeam = snapshot.team.name.isBlank()
            accountLoadFailed = false
            owner.check()
            viewModel.activateSession()
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { accountLoadFailed = true }
        finally { resolvingAccount = false }
    }
    val exportReportToPdf = androidx.compose.runtime.key(session?.id) {
        rememberReportExportAction(viewModel, session)
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> Log.i("FormUpFCM", "Notification permission granted=$granted") }

    LaunchedEffect(signedIn) {
        if (signedIn && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(notificationIntent, signedIn, needsTeam, resolvingAccount, accountLoadFailed, session?.id) {
        if (notificationIntent != null && signedIn && !needsTeam && !resolvingAccount && !accountLoadFailed) {
            viewModel.handleNotificationIntent(notificationIntent)
            onNotificationIntentHandled()
        }
    }

    fun authenticateToUnlock() {

        if (isBiometricPromptActive || isAppUnlocked) {
            return
        }

        val userId = auth.currentUser?.uid

        if (userId == null) {
            viewModel.signOut()
            return
        }

        val activity = context as? FragmentActivity

        if (activity == null) {
            biometricLockMessage = context.getString(R.string.loc_bio_cannot_start)
            return
        }

        if (!BiometricAuth.isBiometricAvailable(context)) {
            biometricLockMessage =
                context.getString(R.string.loc_bio_unavailable)
            return
        }

        isBiometricPromptActive = true

        BiometricAuth.authenticate(
            activity = activity,
            onSuccess = {
                isBiometricPromptActive = false
                isAppUnlocked = true
                biometricLockMessage = context.getString(R.string.loc_coach_protected)
            },
            onError = { error ->
                isBiometricPromptActive = false
                biometricLockMessage = error
            }
        )
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            signedIn = it.currentUser != null
            if (it.currentUser == null) {
                signedIn = false
                needsTeam = false
                showSignUp = false

                isAppUnlocked = true

                if (!passwordFallbackPending) {
                    biometricBypassForSession = false
                }
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    DisposableEffect(
        lifecycleOwner,
        signedIn,
        biometricEnabled,
        biometricBypassForSession
    ) {

        val observer = LifecycleEventObserver { _, event ->

            when (event) {

                Lifecycle.Event.ON_START -> {
                    isAppInForeground = true
                }

                Lifecycle.Event.ON_STOP -> {
                    isAppInForeground = false

                    if (
                        signedIn &&
                        biometricEnabled &&
                        !biometricBypassForSession &&
                        !isBiometricPromptActive
                    ) {
                        isAppUnlocked = false
                    }
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun completeSignIn(block: suspend () -> Boolean) {
        if (authenticating) return
        authError = null
        authenticating = true
        scope.launch {
            try {
                val hasTeam = block()

                needsTeam = !hasTeam
                signedIn = true

                biometricBypassForSession = false
                isAppUnlocked = true

                if (passwordFallbackPending) {
                    passwordFallbackPending = false

                    Toast.makeText(
                        context,
                        context.getString(R.string.loc_password_login_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {

            } catch (e: Exception) {
                Log.e("Auth", "Sign-in failed", e)
                authError = context.getString(com.formup.app.ui.auth.authErrorMessage(e))
            } finally { authenticating = false }
        }
    }

    if (!signedIn) {
        if (showSignUp) {
            SignUpScreen(
                busy = authenticating, authError = authError,
                onCreateAccount = { account: NewAccount ->
                    completeSignIn {
                        emailSignUp(account.fullName, account.email, account.password)
                    }
                },
                onNavigateToLogin = { authError = null; showSignUp = false },
                onGoogleSignUp = { completeSignIn { googleSignIn(context) } }
            )
        } else {
            LoginScreen(
                busy = authenticating, authError = authError,
                onSignIn = { email, password ->
                    completeSignIn { emailSignIn(email, password) }
                },
                onGoogleSignIn = {
                    passwordFallbackPending = false
                    completeSignIn { googleSignIn(context) }
                },
                onNavigateToSignUp = {
                    passwordFallbackPending = false
                    authError = null
                    showSignUp = true
                }
            )
        }
        return
    }

    LaunchedEffect(
        signedIn,
        auth.currentUser?.uid,
        biometricEnabled,
        isAppUnlocked,
        isAppInForeground,
        biometricBypassForSession
    ) {
        if (
            signedIn &&
            biometricEnabled &&
            !biometricBypassForSession &&
            !isAppUnlocked &&
            isAppInForeground
        ) {
            authenticateToUnlock()
        }
    }

    if (
        signedIn &&
        biometricEnabled &&
        !biometricBypassForSession &&
        !isAppUnlocked
    ) {
        BiometricLockScreen(
            message = biometricLockMessage,
            onUnlock = {
                authenticateToUnlock()
            },
            onUsePassword = {

                passwordFallbackPending = true
                biometricBypassForSession = false

                viewModel.signOut()
            }
        )
        return
    }

    if (resolvingAccount || accountLoadFailed) {
        AccountLoadingScreen(resolvingAccount, onRetry = { accountRetry++ }, onSignOut = viewModel::signOut)
        return
    }

    if (needsTeam) {
        CreateTeamScreen(
            onBack = { viewModel.signOut() },
            onCancel = { viewModel.signOut() },
            onTeamCreated = { team ->
                val owner = AccountSessions.requireSession()
                scope.launch {
                    com.formup.app.security.runCatchingCancellable { FormUpApi(session = owner).updateTeam(team.teamName, team.ageGroup); owner.check() }
                        .onSuccess {
                            needsTeam = false
                            viewModel.refreshFromApi()
                        }
                        .onFailure {
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_team_save_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        )
        return
    }

    val activity = context as? ComponentActivity
    BackHandler(enabled = true) {
        if (!viewModel.back()) activity?.finish()
    }

    val hasUnread = viewModel.presentation.unreadCount > 0
    val openNotifications = { viewModel.navigate(Destination.Notifications) }
    val selectTab: (HomeTab) -> Unit = { viewModel.selectTab(it) }

    AppMessageHost(viewModel) {
    FormUpContent(
        viewModel = viewModel,
        darkTheme = darkTheme,
        onDarkThemeChange = onDarkThemeChange,
        biometricEnabled = biometricEnabled,
        onBiometricToggle = { enabled ->
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(context, context.getString(R.string.loc_please_sign_in), Toast.LENGTH_SHORT).show()
            } else if (!enabled) {

                BiometricPreferences.setEnabled(context, userId, false)
                biometricEnabled = false
                Toast.makeText(context, context.getString(R.string.loc_bio_disabled), Toast.LENGTH_SHORT).show()
            } else {

                val fragActivity = context as? FragmentActivity
                if (fragActivity == null) {
                    Toast.makeText(context, context.getString(R.string.loc_auth_error), Toast.LENGTH_SHORT).show()
                } else {
                    BiometricAuth.authenticate(
                        activity = fragActivity,
                        onSuccess = {
                            BiometricPreferences.setEnabled(context, userId, true)
                            biometricEnabled = true
                            Toast.makeText(context, context.getString(R.string.loc_bio_enabled), Toast.LENGTH_SHORT).show()
                        },
                        onError = { errString ->
                            Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        },
        exportReportToPdf = exportReportToPdf,
        hasUnread = hasUnread,
        openNotifications = openNotifications,
        selectTab = selectTab
    )
    }
}
