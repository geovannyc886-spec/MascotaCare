package mx.edu.uttt.mascotacare.ui

import androidx.compose.ui.res.dimensionResource
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme

private data class PetsNavItem(@StringRes val label: Int, val icon: ImageVector)

private val petsNavItems = listOf(
    PetsNavItem(R.string.nav_home, Icons.Outlined.FavoriteBorder),
    PetsNavItem(R.string.nav_pets, Icons.Outlined.AccountCircle),
    PetsNavItem(R.string.nav_appointments, Icons.Outlined.CalendarMonth),
    PetsNavItem(R.string.nav_profile, Icons.Outlined.PersonOutline)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    mascotas: List<Mascota> = emptyList(),
    totalMascotasCount: Int = mascotas.size,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onAddPetClick: () -> Unit = {},
    onPetClick: (Int) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_my_pets), fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.dp_16), end = dimensionResource(R.dimen.dp_8))
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            NavigationBar {
                petsNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == 1,
                        onClick = { onTabSelected(index) },
                        icon = { Icon(item.icon, contentDescription = stringResource(item.label)) },
                        label = { Text(stringResource(item.label)) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPetClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add), fontWeight = FontWeight.Bold) },
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            )
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.pets_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.primary) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = colors.surface.copy(alpha = 0f),
                        unfocusedBorderColor = colors.surface.copy(alpha = 0f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.pets_family),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.pets_count, totalMascotasCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            items(mascotas, key = { it.id }) { pet ->
                PetCard(pet = pet, onClick = { onPetClick(pet.id) })
            }

            if (mascotas.isEmpty()) {
                item {
                    Text(
                        stringResource(
                            if (totalMascotasCount == 0) R.string.pets_empty
                            else R.string.pets_no_search_results
                        ),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(R.dimen.dp_24))
                    )
                }
            }

            item { Spacer(Modifier.height(dimensionResource(R.dimen.dp_72))) }
        }
    }

}

@Composable
private fun PetCard(pet: Mascota, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val ageText = if (pet.fechaNacimiento != null) formatPetAge(pet.fechaNacimiento)
    else stringResource(R.string.pet_age_unknown)
    val infoText = listOf(pet.especie, pet.raza).filter(String::isNotBlank).joinToString(" · ")

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.dp_12)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_paw),
                contentDescription = stringResource(R.string.pets_photo_desc, pet.nombre),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.dp_60))
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(dimensionResource(R.dimen.dp_16)))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(pet.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Text(
                        ageText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Text(
                    infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.pets_saved_status),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(dimensionResource(R.dimen.dp_8)))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun formatPetAge(birthMillis: Long): String {
    val months = ((System.currentTimeMillis() - birthMillis).coerceAtLeast(0L) /
        (30L * 24 * 60 * 60 * 1000)).toInt()
    return if (months >= 12) {
        pluralStringResource(R.plurals.pet_age_years, months / 12, months / 12)
    } else {
        pluralStringResource(R.plurals.pet_age_months, months, months)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PetsScreenPreview() {
    MascotaCareTheme { PetsScreen() }
}
