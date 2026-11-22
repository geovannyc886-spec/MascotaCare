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
) {
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
            "MascotaCare",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Todo el cuidado de tu mascota en un solo lugar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_36)))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = error != null || authMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_16)))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Contraseña") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Ocultar contraseña"
                        else "Mostrar contraseña"
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
            Text("¿Olvidaste tu contraseña?", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_8)))

        Button(
            onClick = {
                error = when {
                    !email.contains("@") -> "Ingresa un correo válido"
                    password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                    else -> null
                }
                if (error == null) {
                    if (creatingAccount) onCreateAccountClick(email, password)
                    else onLoginClick(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.dp_52))
        ) {
            Text(if (creatingAccount) "Crear cuenta" else "Iniciar sesión", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_32)))
        Text(
            if (creatingAccount) "¿Ya tienes cuenta?" else "¿No tienes cuenta?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(dimensionResource(R.dimen.dp_12)))

        OutlinedButton(
            onClick = { creatingAccount = !creatingAccount; error = null },
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.dp_52))
        ) {
            Text(if (creatingAccount) "Iniciar sesión" else "Crear cuenta", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    MascotaCareTheme { LoginScreen(onLoginClick = { _, _ -> }) }
}
