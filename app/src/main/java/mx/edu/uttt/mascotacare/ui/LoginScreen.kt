package mx.edu.uttt.mascotacare.ui

import androidx.compose.ui.res.dimensionResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onCreateAccountClick: (email: String, password: String) -> Unit = { _, _ -> },
    authMessage: String? = null,
    onForgotPasswordClick: () -> Unit = {},
    isLoading: Boolean = false,
    onClearAuthMessage: () -> Unit = {},
) {
    val invalidEmailMessage = stringResource(R.string.login_invalid_email)
    val shortPasswordMessage = stringResource(R.string.login_short_password)
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var creatingAccount by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.dp_32), vertical = dimensionResource(R.dimen.dp_24)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.dp_40)))

        // Logotipo
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.dp_112))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = "Logotipo de MascotaCare",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.dp_60))
            )
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_20)))
        Text(
            stringResource(R.string.login_brand),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_36)))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null; onClearAuthMessage() },
            label = { Text(stringResource(R.string.login_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = error != null || authMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_16)))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null; onClearAuthMessage() },
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = stringResource(if (showPassword) R.string.login_hide_password else R.string.login_show_password)
                    )
                }
            },
            isError = error != null || authMessage != null,
            supportingText = {
                (error ?: authMessage)?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.login_forgot_password), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_8)))

        Button(
            onClick = {
                error = when {
                    !email.contains("@") -> invalidEmailMessage
                    password.length < 6 -> shortPasswordMessage
                    else -> null
                }
                if (error == null) {
                    if (creatingAccount) onCreateAccountClick(email, password)
                    else onLoginClick(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.dp_52)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_20)),
                    strokeWidth = dimensionResource(R.dimen.dp_2)
                )
            } else {
                Text(stringResource(if (creatingAccount) R.string.auth_create_account else R.string.auth_sign_in), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_32)))
        Text(
            stringResource(if (creatingAccount) R.string.login_have_account else R.string.login_no_account),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(dimensionResource(R.dimen.dp_12)))

        OutlinedButton(
            onClick = { creatingAccount = !creatingAccount; error = null; onClearAuthMessage() },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.dp_52))
        ) {
            Text(stringResource(if (creatingAccount) R.string.auth_sign_in else R.string.auth_create_account), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MascotaCareTheme { LoginScreen(onLoginClick = { _, _ -> }) }
}
