// Collects and validates new account details.

package com.formup.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.formup.app.ui.common.FormUpBrand
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.formup.app.ui.auth.components.AuthFieldLabel
import com.formup.app.ui.auth.components.AuthTextField
import com.formup.app.ui.auth.components.GoogleAuthButton
import com.formup.app.ui.auth.components.OrDivider
import com.formup.app.ui.theme.FormUpColors

data class SignUpFormErrors(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null
)

data class NewAccount(
    val fullName: String,
    val email: String,
    val password: String
)

@Composable
fun SignUpScreen(
    onCreateAccount: (NewAccount) -> Unit = {},
    onGoogleSignUp: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    busy: Boolean = false,
    authError: String? = null,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(SignUpFormErrors()) }

    fun submit() {
        if (busy) return
        val nameError = if (fullName.isBlank()) context.getString(R.string.mod_name_required) else null
        val emailError = if (email.isBlank()) context.getString(R.string.mod_email_required) else null
        val passwordError = when {
            password.isBlank() -> context.getString(R.string.mod_password_required)
            password.length < 8 -> context.getString(R.string.mod_password_length)
            else -> null
        }
        if (nameError != null || emailError != null || passwordError != null) {
            errors = SignUpFormErrors(nameError, emailError, passwordError)
            return
        }
        errors = SignUpFormErrors()
        onCreateAccount(NewAccount(fullName.trim(), email.trim(), password))
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
                Column {

                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(FormUpColors.PrimaryDeep)
                    )

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FormUpBrand()
                        com.formup.app.ui.auth.components.AuthFeedback(busy, authError)
                        Spacer(Modifier.height(16.dp))

                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.loc_signup_prompt), style = MaterialTheme.typography.bodyMedium, color = FormUpColors.TextSecondary)

                        Spacer(Modifier.height(22.dp))

                        Column(Modifier.fillMaxWidth()) {
                            AuthFieldLabel(stringResource(R.string.loc_full_name))
                            AuthTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                placeholder = stringResource(R.string.loc_full_name),
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = FormUpColors.TextSecondary) },
                                error = errors.fullName
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Column(Modifier.fillMaxWidth()) {
                            AuthFieldLabel(stringResource(R.string.loc_email_address))
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
                            AuthFieldLabel(stringResource(R.string.loc_password))
                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "••••••••",
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = FormUpColors.TextSecondary) },
                                isPassword = true,
                            onDone = ::submit,
                                error = errors.password,
                                helperText = if (errors.password == null) stringResource(R.string.loc_password_min) else null
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
                            Text(stringResource(R.string.loc_create_account), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.size(8.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }

                        Spacer(Modifier.height(18.dp))
                        OrDivider(stringResource(R.string.loc_or_continue_with))
                        Spacer(Modifier.height(14.dp))
                        GoogleAuthButton(text = stringResource(R.string.loc_signup_google), onClick = onGoogleSignUp, enabled = !busy)

                        Spacer(Modifier.height(18.dp))
                        com.formup.app.ui.auth.components.AuthFooter(
                            stringResource(R.string.loc_have_account), stringResource(R.string.loc_log_in), onNavigateToLogin)
                    }
                }
            }
        }
    }
}
