package mx.edu.uttt.mascotacare.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.data.database.entity.Mascota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    mascota: Mascota,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
) {
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pet_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimensionResource(R.dimen.home_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            Image(
                painter = painterResource(R.drawable.ic_paw),
                contentDescription = stringResource(R.string.pets_photo_desc, mascota.nombre),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(dimensionResource(R.dimen.pet_detail_photo_size))
                    .clip(CircleShape)
            )
            Text(mascota.nombre, style = MaterialTheme.typography.headlineSmall)
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    Text(stringResource(R.string.pet_detail_species, mascota.especie))
                    if (mascota.raza.isNotBlank()) Text(stringResource(R.string.pet_detail_breed, mascota.raza))
                    mascota.fechaNacimiento?.let { Text(stringResource(R.string.pet_detail_birth, formatDate(it))) }
                    mascota.pesoKg?.let { Text(stringResource(R.string.pet_detail_weight, it.toString())) }
                }
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Text(stringResource(R.string.pet_detail_edit), modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_small)))
            }
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text(stringResource(R.string.pet_detail_delete), modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_small)))
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.pet_detail_delete_confirm_title, mascota.nombre)) },
            text = { Text(stringResource(R.string.pet_detail_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.common_delete), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
