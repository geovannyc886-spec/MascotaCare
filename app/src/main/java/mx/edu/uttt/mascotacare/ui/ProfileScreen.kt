package mx.edu.uttt.mascotacare.ui

import androidx.compose.ui.res.dimensionResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    onTabSelected: (Int) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = { MascotaNavigationBar(selectedIndex = 3, onTabSelected = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.dp_24)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.dp_24)))

            // Avatar de usuario
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.dp_100))
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_56))
                )
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_16)))

            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_4)))

            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_40)))

            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(dimensionResource(R.dimen.dp_16))) {
                    Text(
                        stringResource(R.string.nav_profile),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.height(dimensionResource(R.dimen.dp_8)))
                    Text(
                        stringResource(R.string.profile_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_32)))

            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.errorContainer,
                    contentColor = colors.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.dp_52))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(dimensionResource(R.dimen.dp_8)))
                Text(stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(stringResource(R.string.profile_logout), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    MascotaCareTheme { ProfileScreen(userName = "", userEmail = "") }
}
