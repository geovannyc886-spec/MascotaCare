package mx.edu.uttt.mascotacare.ui

import androidx.compose.ui.res.dimensionResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme
import java.util.Calendar

private data class NavItem(@StringRes val label: Int, val icon: ImageVector)

private val navItems = listOf(
    NavItem(R.string.nav_home, Icons.Filled.Home),
    NavItem(R.string.nav_pets, Icons.Filled.Pets),
    NavItem(R.string.nav_appointments, Icons.Filled.CalendarMonth),
    NavItem(R.string.nav_profile, Icons.Filled.Person)
)

private val filters = listOf(
    R.string.filter_all,
    R.string.filter_vaccines,
    R.string.filter_appointments,
    R.string.filter_health
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "",
    mascotas: List<Mascota> = emptyList(),
    proximasCitas: List<Cita> = emptyList(),
    onTabSelected: (Int) -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onAddPetClick: () -> Unit = {},
    onViewDetailClick: (Cita) -> Unit = {}
) {
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }

    val futureAppointments = proximasCitas.filter { it.momentoCita() >= System.currentTimeMillis() }
    val nextAppointment = futureAppointments.minByOrNull { it.momentoCita() }
    val nextAppointmentPet = nextAppointment?.let { cita -> mascotas.find { it.id == cita.mascotaId } }

    val reminderCitas = futureAppointments.filter { it.recordatorio }
    val visibleReminders = if (selectedFilter == 0) reminderCitas
    else reminderCitas.filter { cita ->
        when (selectedFilter) {
            1 -> cita.motivo.contains("vacuna", ignoreCase = true)
            2 -> cita.motivo.contains("cita", ignoreCase = true) || cita.motivo.contains("consulta", ignoreCase = true)
            3 -> cita.motivo.contains("salud", ignoreCase = true) || cita.motivo.contains("desparasit", ignoreCase = true)
            else -> true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_greeting, userName), fontWeight = FontWeight.Bold) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = dimensionResource(R.dimen.dp_16))
                            .size(dimensionResource(R.dimen.dp_36))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName.firstOrNull()?.uppercase()
                                ?: stringResource(R.string.profile_initial_fallback),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == 0,
                        onClick = { onTabSelected(index) },
                        icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                        label = { Text(stringResource(item.label)) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScheduleClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_schedule_appointment))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.dp_20), vertical = dimensionResource(R.dimen.dp_8)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_16))
        ) {
            item {
                NextAppointmentCard(
                    cita = nextAppointment,
                    pet = nextAppointmentPet,
                    onViewDetailClick = { cita -> onViewDetailClick(cita) }
                )
            }

            item { SectionTitle(stringResource(R.string.home_my_pets)) }
            item {
                if (mascotas.isEmpty()) {
                    Text(
                        stringResource(R.string.home_no_pets),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8))
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_20))) {
                        items(mascotas, key = { it.id }) { pet ->
                            PetAvatar(pet)
                        }
                        item { AddPetButton(onClick = onAddPetClick) }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_8))) {
                    items(filters.size) { i ->
                        FilterChip(
                            selected = selectedFilter == i,
                            onClick = { selectedFilter = i },
                            label = { Text(stringResource(filters[i])) },
                            leadingIcon = if (selectedFilter == i) {
                                { Icon(Icons.Filled.Check, null, Modifier.size(dimensionResource(R.dimen.dp_18))) }
                            } else null
                        )
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.home_reminders)) }

            if (visibleReminders.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.home_no_reminders),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8))
                    )
                }
            } else {
                items(visibleReminders, key = { it.id }) { cita ->
                    val pet = mascotas.find { it.id == cita.mascotaId }
                    ReminderCitaItem(cita = cita, pet = pet, onClick = { onViewDetailClick(cita) })
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun NextAppointmentCard(
    cita: Cita?,
    pet: Mascota?,
    onViewDetailClick: (Cita) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.dp_20)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_next_appointment), style = MaterialTheme.typography.labelLarge)
                if (cita != null) {
                    Text(
                        cita.motivo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    val petName = pet?.nombre ?: ""
                    val hour12 = if (cita.hora % 12 == 0) 12 else cita.hora % 12
                    val period = stringResource(if (cita.hora < 12) R.string.time_am else R.string.time_pm)
                    val timeText = stringResource(R.string.time_format, hour12, cita.minuto, period)
                    val dateText = formatDate(cita.fecha)
                    Text("$petName · $dateText · $timeText", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(dimensionResource(R.dimen.dp_12)))
                    Button(onClick = { onViewDetailClick(cita) }) { Text(stringResource(R.string.home_view_detail)) }
                } else {
                    Text(
                        stringResource(R.string.home_no_appointments),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8))
                    )
                }
            }
            Icon(
                Icons.Filled.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.dp_72))
            )
        }
    }
}

@Composable
private fun PetAvatar(pet: Mascota) {
    val avatarColor = when (pet.id % 3) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(dimensionResource(R.dimen.dp_60))
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                pet.nombre.firstOrNull()?.uppercase().orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.dp_6)))
        Text(pet.nombre, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AddPetButton(onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            border = BorderStroke(dimensionResource(R.dimen.dp_1), MaterialTheme.colorScheme.outline),
            modifier = Modifier.size(dimensionResource(R.dimen.dp_60))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.home_add),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.dp_6)))
        Text(stringResource(R.string.home_add), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReminderCitaItem(cita: Cita, pet: Mascota?, onClick: () -> Unit) {
    val hour12 = if (cita.hora % 12 == 0) 12 else cita.hora % 12
    val period = stringResource(if (cita.hora < 12) R.string.time_am else R.string.time_pm)
    val timeText = stringResource(R.string.time_format, hour12, cita.minuto, period)
    val dateText = formatDate(cita.fecha)
    val petName = pet?.nombre ?: ""

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(cita.motivo, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("$petName · $dateText · $timeText") },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.dp_40))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_20))
                )
            }
        },
        trailingContent = {
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f))
    )
}

private fun Cita.momentoCita(): Long = Calendar.getInstance().apply {
    timeInMillis = fecha
    set(Calendar.HOUR_OF_DAY, hora)
    set(Calendar.MINUTE, minuto)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MascotaCareTheme { HomeScreen(userName = "") }
}
