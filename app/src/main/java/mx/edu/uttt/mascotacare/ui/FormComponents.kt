package mx.edu.uttt.mascotacare.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import mx.edu.uttt.mascotacare.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Campo de solo lectura que reacciona al toque (para fecha, hora y menús). */
@Composable
fun ClickableField(
    label: String,
    value: String,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { Icon(trailingIcon, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
        extraContent()
    }
}

/** Campo con menú desplegable. */
@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ClickableField(
        label = label,
        value = value,
        trailingIcon = Icons.Filled.KeyboardArrowDown,
        onClick = { expanded = true },
        modifier = modifier
    ) {
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.selectedDateMillis) }) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
        text = { TimePicker(state = state) }
    )
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))
