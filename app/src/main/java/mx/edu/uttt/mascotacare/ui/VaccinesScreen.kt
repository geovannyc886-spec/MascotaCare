package mx.edu.uttt.mascotacare.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.domain.model.Cita
import mx.edu.uttt.mascotacare.domain.model.Mascota
import mx.edu.uttt.mascotacare.domain.model.Vacuna
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme
import mx.edu.uttt.mascotacare.util.dateTimeAtLocalTime
import mx.edu.uttt.mascotacare.util.sameUtcDay
import mx.edu.uttt.mascotacare.util.utcCalendar
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinesScreen(
    mascotas: List<Mascota> = emptyList(),
    vacunas: List<Vacuna> = emptyList(),
    citas: List<Cita> = emptyList(),
    selectedMascotaId: Int? = null,
    onMascotaSelect: (Int?) -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onAddClick: () -> Unit = {},
    onSaveVacuna: (Vacuna) -> Unit = {},
    onUpdateVacuna: (Vacuna) -> Unit = {},
    onDeleteVacuna: (Vacuna) -> Unit = {},
    onCitaClick: (Cita) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    // Calender month & year state
    var year by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selYear by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selMonth by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selDay by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var dayFilterActive by rememberSaveable { mutableStateOf(false) }
    var editingVacuna by remember { mutableStateOf<Vacuna?>(null) }
    var showVacunaDialog by rememberSaveable { mutableStateOf(false) }

    // Auto-select first pet if none selected
    LaunchedEffect(mascotas) {
        if (mascotas.none { it.id == selectedMascotaId }) {
            onMascotaSelect(mascotas.firstOrNull()?.id)
        }
    }

    val selectedPet = mascotas.find { it.id == selectedMascotaId } ?: mascotas.firstOrNull()

    // Filter citas by selected day if day filter active
    val filteredVacunas = if (dayFilterActive) {
        vacunas.filter { vacuna ->
            sameUtcDay(vacuna.fecha, selYear, selMonth, selDay)
        }
    } else vacunas

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vaccines_title)) },
                navigationIcon = {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.dp_16), end = dimensionResource(R.dimen.dp_8))
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = { MascotaNavigationBar(selectedIndex = 2, onTabSelected = onTabSelected) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingVacuna = null; showVacunaDialog = true },
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.vaccine_add_title))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(R.dimen.home_padding), vertical = dimensionResource(R.dimen.spacing_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            if (mascotas.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.dp_16))
                ) {
                    Text(
                        stringResource(R.string.vaccines_no_pets),
                        modifier = Modifier.padding(dimensionResource(R.dimen.dp_20)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                // Selector de mascota con SegmentedButton
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    mascotas.forEachIndexed { index, pet ->
                        SegmentedButton(
                            selected = pet.id == selectedPet?.id,
                            onClick = {
                                onMascotaSelect(pet.id)
                                dayFilterActive = false
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = mascotas.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = colors.primaryContainer,
                                activeContentColor = colors.onPrimaryContainer
                            )
                        ) {
                            Text(pet.nombre, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            CalendarCard(
                year = year,
                month = month,
                selYear = selYear,
                selMonth = selMonth,
                selDay = selDay,
                vacunas = vacunas,
                onPrev = {
                    dayFilterActive = false
                    if (month == 0) { month = 11; year-- } else month--
                },
                onNext = {
                    dayFilterActive = false
                    if (month == 11) { month = 0; year++ } else month++
                },
                onSelectDay = { day ->
                    if (dayFilterActive && selDay == day && selMonth == month && selYear == year) {
                        dayFilterActive = false
                    } else {
                        selYear = year
                        selMonth = month
                        selDay = day
                        dayFilterActive = true
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.vaccines_of, selectedPet?.nombre ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(R.string.vaccines_records, filteredVacunas.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            if (filteredVacunas.isEmpty()) {
                Text(
                    stringResource(R.string.vaccines_no_records),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_12))
                )
            } else {
                filteredVacunas.forEach { vacuna ->
                    VaccineCard(
                        vacuna = vacuna,
                        onEdit = { editingVacuna = vacuna; showVacunaDialog = true },
                        onToggleApplied = { onUpdateVacuna(vacuna.copy(aplicada = !vacuna.aplicada)) },
                        onDelete = { onDeleteVacuna(vacuna) }
                    )
                }
            }

            if (citas.isNotEmpty()) {
                Text(stringResource(R.string.vaccines_appointments), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                citas.filter { !dayFilterActive || sameUtcDay(it.fecha, selYear, selMonth, selDay) }
                    .forEach { cita ->
                        Card(Modifier.fillMaxWidth().clickable { onCitaClick(cita) }, colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
                            ListItem(headlineContent = { Text(cita.motivo) }, supportingContent = { Text(formatDate(cita.fecha)) }, trailingContent = { Text(stringResource(R.string.common_edit)) }, colors = ListItemDefaults.colors(containerColor = colors.surfaceVariant))
                        }
                    }
            } else if (mascotas.isNotEmpty()) {
                Text(stringResource(R.string.vaccines_no_appointments), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_72)))
        }
    }

    if (showVacunaDialog) {
        VaccineDialog(
            mascotaId = selectedPet?.id,
            vacuna = editingVacuna,
            onDismiss = { showVacunaDialog = false; editingVacuna = null },
            onSave = { vacuna ->
                if (editingVacuna == null) onSaveVacuna(vacuna) else onUpdateVacuna(vacuna)
                showVacunaDialog = false; editingVacuna = null
            }
        )
    }
}

@Composable
private fun CalendarCard(
    year: Int,
    month: Int,
    selYear: Int,
    selMonth: Int,
    selDay: Int,
    vacunas: List<Vacuna>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val months = stringArrayResource(R.array.months)
    val weekdays = stringArrayResource(R.array.weekdays)

    val cal = Calendar.getInstance().apply { clear(); set(year, month, 1) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val offset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val weeks = (offset + daysInMonth + 6) / 7

    // Days with citas in this month
    val daysWithCitas = vacunas.mapNotNull { vacuna ->
        val c = utcCalendar(vacuna.fecha)
        if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
            c.get(Calendar.DAY_OF_MONTH)
        } else null
    }.toSet()

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(dimensionResource(R.dimen.dp_12))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev) {
                    Icon(Icons.Filled.ChevronLeft, stringResource(R.string.calendar_prev), tint = colors.primary)
                }
                Text(
                    text = stringResource(R.string.calendar_month_year, months[month], year),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.ChevronRight, stringResource(R.string.calendar_next), tint = colors.primary)
                }
            }

            Row(Modifier.fillMaxWidth()) {
                weekdays.forEach { d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.dp_4)))

            for (week in 0 until weeks) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val day = week * 7 + col - offset + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensionResource(R.dimen.dp_40)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                val isSelected = day == selDay && month == selMonth && year == selYear
                                val hasCita = daysWithCitas.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(dimensionResource(R.dimen.dp_36))
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> colors.primary
                                                hasCita -> colors.secondaryContainer
                                                else -> colors.surface.copy(alpha = 0f)
                                            }
                                        )
                                        .clickable { onSelectDay(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isSelected -> colors.onPrimary
                                            hasCita -> colors.onSecondaryContainer
                                            else -> colors.onSurface
                                        },
                                        fontWeight = if (isSelected || hasCita) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitaCard(cita: Cita) {
    val colors = MaterialTheme.colorScheme
    val isPast = dateTimeAtLocalTime(cita.fecha, cita.hora, cita.minuto) < System.currentTimeMillis()
    val hour12 = if (cita.hora % 12 == 0) 12 else cita.hora % 12
    val period = stringResource(if (cita.hora < 12) R.string.time_am else R.string.time_pm)
    val timeStr = stringResource(R.string.time_format, hour12, cita.minuto, period)
    val dateStr = formatDate(cita.fecha)

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.dp_16)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(cita.motivo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "$dateStr · $timeStr${if (cita.clinica.isNotBlank()) " · ${cita.clinica}" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(dimensionResource(R.dimen.dp_8)))
            StatusChip(isPast = isPast)
        }
    }
}

@Composable
private fun StatusChip(isPast: Boolean) {
    val colors = MaterialTheme.colorScheme
    if (!isPast) {
        Surface(
            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_8)),
            color = colors.background,
            border = BorderStroke(dimensionResource(R.dimen.dp_1), colors.outlineVariant)
        ) {
            Text(
                stringResource(R.string.vaccine_pending),
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_16), vertical = dimensionResource(R.dimen.dp_10)),
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        Surface(
            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_8)),
            color = colors.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.dp_16), vertical = dimensionResource(R.dimen.dp_10)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Check, null, Modifier.size(dimensionResource(R.dimen.dp_18)), tint = colors.onPrimaryContainer)
                Spacer(Modifier.width(dimensionResource(R.dimen.dp_6)))
                Text(
                    stringResource(R.string.vaccine_applied),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VaccineCard(vacuna: Vacuna, onEdit: () -> Unit, onToggleApplied: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var confirmDelete by rememberSaveable(vacuna.id) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
        Column(Modifier.padding(dimensionResource(R.dimen.dp_16))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(vacuna.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${formatDate(vacuna.fecha)} · ${vacuna.dosis}", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onEdit) { Text(stringResource(R.string.common_edit)) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (vacuna.aplicada) stringResource(R.string.vaccine_applied) else stringResource(R.string.vaccine_pending), Modifier.weight(1f), color = colors.primary)
                TextButton(onClick = onToggleApplied) { Text(stringResource(if (vacuna.aplicada) R.string.vaccine_mark_pending else R.string.vaccine_mark_applied)) }
                TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.common_delete), color = colors.error) }
            }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text(stringResource(R.string.vaccine_delete_title)) },
        text = { Text(stringResource(R.string.vaccine_delete_message, vacuna.nombre)) },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(stringResource(R.string.common_delete), color = colors.error) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Composable
private fun VaccineDialog(mascotaId: Int?, vacuna: Vacuna?, onDismiss: () -> Unit, onSave: (Vacuna) -> Unit) {
    var name by rememberSaveable(vacuna?.id) { mutableStateOf(vacuna?.nombre.orEmpty()) }
    var dose by rememberSaveable(vacuna?.id) { mutableStateOf(vacuna?.dosis.orEmpty()) }
    var date by rememberSaveable(vacuna?.id) { mutableStateOf(vacuna?.fecha ?: System.currentTimeMillis()) }
    var applied by rememberSaveable(vacuna?.id) { mutableStateOf(vacuna?.aplicada ?: false) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (vacuna == null) R.string.vaccine_add_title else R.string.vaccine_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_8))) {
                OutlinedTextField(name, { name = it; invalid = false }, label = { Text(stringResource(R.string.vaccine_name)) }, singleLine = true)
                OutlinedTextField(dose, { dose = it; invalid = false }, label = { Text(stringResource(R.string.vaccine_dose)) }, singleLine = true)
                ClickableField(stringResource(R.string.appt_date), formatDate(date), Icons.Filled.CalendarMonth, { showDate = true })
                Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.vaccine_applied), Modifier.weight(1f)); Checkbox(applied, { applied = it }) }
                if (invalid) Text(stringResource(R.string.vaccine_fields_required), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { if (mascotaId == null || name.isBlank() || dose.isBlank()) invalid = true else onSave(Vacuna(vacuna?.id ?: 0, mascotaId, name.trim(), date, dose.trim(), applied)) }) { Text(stringResource(R.string.common_accept)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    )
    if (showDate) AppDatePickerDialog(date, { showDate = false }, { it?.let { value -> date = value }; showDate = false })
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VaccinesScreenPreview() {
    MascotaCareTheme { VaccinesScreen() }
}
