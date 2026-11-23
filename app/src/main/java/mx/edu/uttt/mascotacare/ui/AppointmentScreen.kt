package mx.edu.uttt.mascotacare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.domain.model.Mascota
import mx.edu.uttt.mascotacare.domain.model.Cita
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme
import mx.edu.uttt.mascotacare.util.dateTimeAtLocalTime
import mx.edu.uttt.mascotacare.util.isDateBeforeToday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(
    mascotas: List<Mascota> = emptyList(),
    onBack: () -> Unit = {},
    onDone: (mascotaId: Int, motivo: String, fecha: Long, hora: Int, minuto: Int, clinica: String, recordatorio: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onAddPetClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    cita: Cita? = null,
    onUpdate: (Cita) -> Unit = {},
    onDelete: (Cita) -> Unit = {},
    onClearMessage: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    var selectedPetId by rememberSaveable(cita?.id) { mutableStateOf<Int?>(cita?.mascotaId) }
    var petMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var reason by rememberSaveable(cita?.id) { mutableStateOf(cita?.motivo.orEmpty()) }
    var clinic by rememberSaveable(cita?.id) { mutableStateOf(cita?.clinica.orEmpty()) }
    var dateMillis by rememberSaveable(cita?.id) { mutableStateOf<Long?>(cita?.fecha ?: System.currentTimeMillis()) }
    var hour by rememberSaveable(cita?.id) { mutableIntStateOf(cita?.hora ?: 10) }
    var minute by rememberSaveable(cita?.id) { mutableIntStateOf(cita?.minuto ?: 30) }
    var reminder by rememberSaveable(cita?.id) { mutableStateOf(cita?.recordatorio ?: true) }
    var reasonError by rememberSaveable { mutableStateOf(false) }
    var dateError by rememberSaveable { mutableStateOf(false) }
    var timeError by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    val period = stringResource(if (hour < 12) R.string.time_am else R.string.time_pm)
    val timeText = stringResource(R.string.time_format, hour12, minute, period)

    LaunchedEffect(mascotas) {
        if (selectedPetId == null || mascotas.none { it.id == selectedPetId }) {
            selectedPetId = mascotas.firstOrNull()?.id
        }
    }
    val selectedPet = mascotas.firstOrNull { it.id == selectedPetId } ?: mascotas.firstOrNull()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (cita == null) R.string.appt_title else R.string.appt_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = { MascotaNavigationBar(selectedIndex = 2, onTabSelected = onTabSelected) },
    ) { padding ->
        if (mascotas.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(dimensionResource(R.dimen.dp_24)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_64))
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.dp_16)))
                Text(
                    stringResource(R.string.appt_no_pets_warning),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_16))
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.dp_24)))
                Button(
                    onClick = onAddPetClick,
                    modifier = Modifier.height(dimensionResource(R.dimen.dp_52))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(dimensionResource(R.dimen.dp_8)))
                    Text(stringResource(R.string.appt_add_pet_btn), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = dimensionResource(R.dimen.dp_24), vertical = dimensionResource(R.dimen.spacing_small)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                Text(
                    stringResource(R.string.appt_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                ClickableField(
                    label = stringResource(R.string.appt_pet),
                    value = selectedPet?.nombre ?: "",
                    trailingIcon = Icons.Filled.KeyboardArrowDown,
                    onClick = { petMenuExpanded = true }
                ) {
                    DropdownMenu(
                        expanded = petMenuExpanded,
                        onDismissRequest = { petMenuExpanded = false }
                    ) {
                        mascotas.forEach { pet ->
                            DropdownMenuItem(
                                text = { Text(pet.nombre) },
                                onClick = {
                                    selectedPetId = pet.id
                                    petMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it; reasonError = false; onClearMessage() },
                    label = { Text(stringResource(R.string.appt_reason)) },
                    singleLine = true,
                    isError = reasonError,
                    supportingText = { if (reasonError) Text(stringResource(R.string.appt_reason_error)) },
                    modifier = Modifier.fillMaxWidth()
                )

                ClickableField(
                    label = stringResource(R.string.appt_date),
                    value = dateMillis?.let { formatDate(it) } ?: "",
                    trailingIcon = Icons.Filled.CalendarMonth,
                    onClick = { showDatePicker = true; dateError = false }
                )
                if (dateError) Text(stringResource(R.string.appt_date_past), color = colors.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))

                ClickableField(
                    label = stringResource(R.string.appt_time),
                    value = timeText,
                    trailingIcon = Icons.Filled.AccessTime,
                    onClick = { showTimePicker = true; timeError = false }
                )
                if (timeError) Text(stringResource(R.string.appt_time_past), color = colors.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))

                OutlinedTextField(
                    value = clinic,
                onValueChange = { clinic = it; onClearMessage() },
                    label = { Text(stringResource(R.string.appt_clinic)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.dp_16), vertical = dimensionResource(R.dimen.dp_12)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.appt_reminder),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(checked = reminder, onCheckedChange = { reminder = it })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_cancel)) }
                    if (cita != null) TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.common_delete), color = colors.error) }
                    Spacer(Modifier.width(dimensionResource(R.dimen.dp_16)))
                    Button(
                        onClick = {
                            if (reason.isBlank()) {
                                reasonError = true
                            } else if (selectedPet != null) {
                                val selectedDate = dateMillis ?: 0L
                                val now = System.currentTimeMillis()
                                val appointmentMoment = dateTimeAtLocalTime(selectedDate, hour, minute)
                                dateError = selectedDate == 0L || isDateBeforeToday(selectedDate)
                                timeError = !dateError && appointmentMoment < now
                                if (!dateError && !timeError) {
                                    if (cita == null) onDone(selectedPet.id, reason.trim(), selectedDate, hour, minute, clinic.trim(), reminder)
                                    else onUpdate(cita.copy(
                                        mascotaId = selectedPet.id, motivo = reason.trim(), fecha = selectedDate,
                                        hora = hour, minuto = minute, clinica = clinic.trim(), recordatorio = reminder
                                    ))
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(dimensionResource(R.dimen.button_height))
                    ) {
                        Text(stringResource(if (cita == null) R.string.appt_confirm else R.string.appt_save_changes))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = dateMillis,
            onDismiss = { showDatePicker = false },
                    onConfirm = { dateMillis = it; showDatePicker = false; dateError = false; onClearMessage() }
        )
    }
    if (showTimePicker) {
        AppTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m -> hour = h; minute = m; showTimePicker = false; timeError = false; onClearMessage() }
        )
    }
    if (confirmDelete && cita != null) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(stringResource(R.string.appt_delete_title)) },
        text = { Text(stringResource(R.string.appt_delete_message, cita.motivo)) },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(cita) }) { Text(stringResource(R.string.common_delete), color = colors.error) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppointmentScreenPreview() {
    MascotaCareTheme { AppointmentScreen() }
}
