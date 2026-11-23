// Collects sign-in details and validates the form before submission.

package com.formup.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.formup.app.ui.common.FormUpBrand
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.formup.app.R
import com.formup.app.ui.auth.components.AuthTextField
import com.formup.app.ui.auth.components.GoogleAuthButton
import com.formup.app.ui.auth.components.OrDivider
import com.formup.app.ui.theme.FormUpColors

data class LoginFormErrors(
    val email: String? = null,
    val password: String? = null
)

@Composable
fun LoginScreen(

    onSignIn: (email: String, password: String) -> Unit = { _, _ -> },

    onGoogleSignIn: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    busy: Boolean = false,
    authError: String? = null,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(LoginFormErrors()) }

    fun submit() {
        if (busy) return
        val emailError = if (email.isBlank()) context.getString(R.string.mod_email_required) else null
        val passwordError = if (password.isBlank()) context.getString(R.string.mod_password_required) else null

        if (emailError != null || passwordError != null) {
            errors = LoginFormErrors(emailError, passwordError)
            return
        }

        errors = LoginFormErrors()
        onSignIn(email.trim(), password)
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FormUpColors.Background
    ) { inner ->

        com.formup.app.ui.auth.components.AuthPageFrame(Modifier.padding(inner)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = FormUpColors.Surface,
                border = BorderStroke(1.dp, FormUpColors.Hairline)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    FormUpBrand()
                    com.formup.app.ui.auth.components.AuthFeedback(busy, authError)

                    Spacer(Modifier.height(16.dp))

                    Text(stringResource(R.string.loc_welcome_back), style = MaterialTheme.typography.headlineMedium, color = FormUpColors.Primary)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.loc_signin_prompt), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)

                    Spacer(Modifier.height(20.dp))

                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.loc_email_address), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = stringResource(R.string.loc_email_address),
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = FormUpColors.TextSecondary) },
                            keyboardType = KeyboardType.Email,
                            error = errors.email
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.loc_password), style = MaterialTheme.typography.bodySmall, color = FormUpColors.TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
                        AuthTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "••••••••",
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = FormUpColors.TextSecondary) },
                            isPassword = true,
                            onDone = ::submit,
                            error = errors.password
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    com.formup.app.ui.common.FormUpButton(
                        onClick = ::submit,
                            enabled = !busy,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FormUpColors.ButtonFill, contentColor = FormUpColors.OnButton),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text(stringResource(R.string.loc_sign_in), style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(18.dp))

                    OrDivider(stringResource(R.string.loc_or_continue_with))

                    Spacer(Modifier.height(14.dp))

                    GoogleAuthButton(text = "Google", onClick = onGoogleSignIn, enabled = !busy)

                    Spacer(Modifier.height(18.dp))

                    com.formup.app.ui.auth.components.AuthFooter(
                        stringResource(R.string.loc_no_account), stringResource(R.string.loc_sign_up), onNavigateToSignUp)
                }
            }
        }
    }
}
