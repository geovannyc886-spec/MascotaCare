package mx.edu.uttt.mascotacare.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.ui.theme.MascotaCareTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    onBack: () -> Unit = {},
    onSave: (nombre: String, especie: String, raza: String, fechaNacimiento: Long?, pesoKg: Double?) -> Unit = { _, _, _, _, _ -> },
    onTabSelected: (Int) -> Unit = {},
    mascota: Mascota? = null,
    errorMessage: String? = null,
    isLoading: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme

    var name by rememberSaveable(mascota?.id) { mutableStateOf(mascota?.nombre.orEmpty()) }
    var species by rememberSaveable(mascota?.id) { mutableStateOf(mascota?.especie.orEmpty()) }
    var breed by rememberSaveable(mascota?.id) { mutableStateOf(mascota?.raza.orEmpty()) }
    var weight by rememberSaveable(mascota?.id) { mutableStateOf(mascota?.pesoKg?.toString().orEmpty()) }
    var birthMillis by rememberSaveable(mascota?.id) { mutableStateOf(mascota?.fechaNacimiento) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var nameTooLong by rememberSaveable { mutableStateOf(false) }
    var speciesError by rememberSaveable { mutableStateOf(false) }

    val speciesOptions = listOf(
        stringResource(R.string.species_dog),
        stringResource(R.string.species_cat),
        stringResource(R.string.species_rabbit),
        stringResource(R.string.species_bird),
        stringResource(R.string.species_other)
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (mascota == null) R.string.add_pet_title else R.string.edit_pet_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = { MascotaNavigationBar(selectedIndex = 1, onTabSelected = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(R.dimen.dp_24), vertical = dimensionResource(R.dimen.spacing_small)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            // Foto de la mascota
            Box(Modifier.size(dimensionResource(R.dimen.dp_88))) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_paw),
                        contentDescription = stringResource(R.string.add_pet_photo_desc),
                        tint = colors.primary,
                        modifier = Modifier.size(dimensionResource(R.dimen.dp_44))
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(dimensionResource(R.dimen.dp_32))
                        .clip(RoundedCornerShape(dimensionResource(R.dimen.dp_10)))
                        .background(colors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = colors.primary, modifier = Modifier.size(dimensionResource(R.dimen.dp_18)))
                }
            }
            Text(
                stringResource(R.string.add_pet_photo_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    if (it.length <= 20) {
                        name = it
                        nameTooLong = false
                    } else {
                        nameTooLong = true
                    }
                    nameError = false
                },
                label = { Text(stringResource(R.string.add_pet_name)) },
                singleLine = true,
                isError = nameError || nameTooLong,
                supportingText = {
                    when {
                        nameTooLong -> Text(stringResource(R.string.add_pet_name_max_error))
                        nameError -> Text(stringResource(R.string.add_pet_name_error))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownField(
                label = stringResource(R.string.add_pet_species),
                value = species,
                options = speciesOptions,
                onSelected = { species = it; speciesError = false }
            )
            if (speciesError) {
                Text(
                    stringResource(R.string.add_pet_species_error),
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            errorMessage?.let { message ->
                val text = when (message) {
                    "add_pet_name_required" -> stringResource(R.string.add_pet_name_error)
                    "add_pet_name_max_error" -> stringResource(R.string.add_pet_name_max_error)
                    else -> message
                }
                Text(text, color = colors.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text(stringResource(R.string.add_pet_breed)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ClickableField(
                label = stringResource(R.string.add_pet_birth),
                value = birthMillis?.let { formatDate(it) } ?: "",
                trailingIcon = Icons.Filled.CalendarMonth,
                onClick = { showDatePicker = true }
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(stringResource(R.string.add_pet_weight)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.common_cancel)) }
                Spacer(Modifier.width(dimensionResource(R.dimen.dp_16)))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else if (nameTooLong) {
                            nameError = false
                        } else if (species.isBlank()) {
                            speciesError = true
                        } else {
                            val pesoDouble = weight.replace(',', '.').toDoubleOrNull()
                            onSave(name.trim(), species, breed.trim(), birthMillis, pesoDouble)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(dimensionResource(R.dimen.button_height)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(dimensionResource(R.dimen.dp_20)),
                            strokeWidth = dimensionResource(R.dimen.dp_2)
                        )
                    } else {
                        Text(stringResource(if (mascota == null) R.string.add_pet_save else R.string.edit_pet_save))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = birthMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { birthMillis = it; showDatePicker = false }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddPetScreenPreview() {
    MascotaCareTheme { AddPetScreen() }
}
