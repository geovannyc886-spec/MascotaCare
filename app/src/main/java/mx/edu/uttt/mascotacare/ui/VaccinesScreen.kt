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
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinesScreen(
    mascotas: List<Mascota> = emptyList(),
    citas: List<Cita> = emptyList(),
    selectedMascotaId: Int? = null,
    onMascotaSelect: (Int?) -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    // Calender month & year state
    var year by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selYear by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selMonth by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selDay by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var dayFilterActive by rememberSaveable { mutableStateOf(false) }

    // Auto-select first pet if none selected
    LaunchedEffect(mascotas) {
        if (mascotas.none { it.id == selectedMascotaId }) {
            onMascotaSelect(mascotas.firstOrNull()?.id)
        }
    }

    val selectedPet = mascotas.find { it.id == selectedMascotaId } ?: mascotas.firstOrNull()

    // Filter citas by selected day if day filter active
    val filteredCitas = if (dayFilterActive) {
        citas.filter { cita ->
            val cal = Calendar.getInstance().apply { timeInMillis = cita.fecha }
            cal.get(Calendar.YEAR) == selYear &&
                cal.get(Calendar.MONTH) == selMonth &&
                cal.get(Calendar.DAY_OF_MONTH) == selDay
        }
    } else {
        citas
    }

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
                onClick = onAddClick,
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_schedule_appointment))
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
                citas = citas,
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
                    stringResource(R.string.vaccines_records, filteredCitas.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            if (filteredCitas.isEmpty()) {
                Text(
                    stringResource(R.string.vaccines_no_citas),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_12))
                )
            } else {
                filteredCitas.forEach { cita -> CitaCard(cita) }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.dp_72)))
        }
    }
}

@Composable
private fun CalendarCard(
    year: Int,
    month: Int,
    selYear: Int,
    selMonth: Int,
    selDay: Int,
    citas: List<Cita>,
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
    val daysWithCitas = citas.mapNotNull { cita ->
        val c = Calendar.getInstance().apply { timeInMillis = cita.fecha }
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
    val isPast = Calendar.getInstance().run {
        timeInMillis = cita.fecha
        set(Calendar.HOUR_OF_DAY, cita.hora)
        set(Calendar.MINUTE, cita.minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis < System.currentTimeMillis()
    }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VaccinesScreenPreview() {
    MascotaCareTheme { VaccinesScreen() }
}
