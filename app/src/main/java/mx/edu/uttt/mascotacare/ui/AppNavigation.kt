package mx.edu.uttt.mascotacare.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import mx.edu.uttt.mascotacare.R
import mx.edu.uttt.mascotacare.ui.viewmodel.AuthViewModel
import mx.edu.uttt.mascotacare.ui.viewmodel.CitaViewModel
import mx.edu.uttt.mascotacare.ui.viewmodel.MascotaViewModel
import mx.edu.uttt.mascotacare.ui.viewmodel.VacunaViewModel

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PETS = "pets"
    const val ADD_PET = "add_pet?mascotaId={mascotaId}"
    const val VACCINES = "vaccines"
    const val APPOINTMENT = "appointment"
    const val APPOINTMENT_EDIT = "appointment?citaId={citaId}"
    const val PROFILE = "profile"
    const val PET_DETAIL = "pet_detail/{petId}"
    fun petDetail(petId: Int) = "pet_detail/$petId"
    fun addPet(mascotaId: Int? = null) =
        if (mascotaId == null) "add_pet" else "add_pet?mascotaId=$mascotaId"
    fun appointment(citaId: Int? = null) = if (citaId == null) APPOINTMENT else "appointment?citaId=$citaId"
}

private data class PendingUiMessage(val source: String, val code: String, val error: Boolean)

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    mascotaViewModel: MascotaViewModel = hiltViewModel(),
    citaViewModel: CitaViewModel = hiltViewModel(),
    vacunaViewModel: VacunaViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val accountExistsMessage = stringResource(R.string.auth_account_exists)
    val authErrorMessage = stringResource(R.string.auth_unavailable)
    val authMessage = when (authState.errorMessage) {
        "auth_email_not_found" -> stringResource(R.string.auth_email_not_found)
        "auth_wrong_password" -> stringResource(R.string.auth_wrong_password)
        "auth_account_exists" -> accountExistsMessage
        "auth_unavailable" -> authErrorMessage
        "database_error" -> stringResource(R.string.database_error)
        "database_load_error" -> stringResource(R.string.database_load_error)
        "auth_fields_required" -> stringResource(R.string.auth_fields_required)
        "auth_weak_password" -> stringResource(R.string.auth_weak_password)
        else -> null
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val mascotaState by mascotaViewModel.uiState.collectAsStateWithLifecycle()
    val mascotas = mascotaState.mascotas
    val filteredMascotas = mascotaState.mascotasFiltradas
    val searchQuery = mascotaState.searchQuery

    val citaState by citaViewModel.uiState.collectAsStateWithLifecycle()
    val proximasCitas = citaState.proximasCitas
    val citasDeMascotaSeleccionada = citaState.citasDeMascota
    val selectedMascotaId = citaState.selectedMascotaId
    val vacunaState by vacunaViewModel.uiState.collectAsStateWithLifecycle()

    val localizedMessages = mapOf(
        "auth_email_not_found" to stringResource(R.string.auth_email_not_found),
        "auth_wrong_password" to stringResource(R.string.auth_wrong_password),
        "auth_account_exists" to accountExistsMessage,
        "auth_unavailable" to authErrorMessage,
        "auth_fields_required" to stringResource(R.string.auth_fields_required),
        "auth_weak_password" to stringResource(R.string.auth_weak_password),
        "auth_password_changed" to stringResource(R.string.auth_password_changed),
        "auth_account_deleted" to stringResource(R.string.auth_account_deleted),
        "database_error" to stringResource(R.string.database_error),
        "database_load_error" to stringResource(R.string.database_load_error),
        "pet_name_required" to stringResource(R.string.pet_name_required_message),
        "pet_name_too_long" to stringResource(R.string.pet_name_too_long_message),
        "pet_weight_invalid" to stringResource(R.string.pet_weight_invalid),
        "pet_birth_future" to stringResource(R.string.pet_birth_future),
        "pet_duplicate" to stringResource(R.string.pet_duplicate),
        "pet_saved" to stringResource(R.string.pet_saved),
        "pet_updated" to stringResource(R.string.pet_updated),
        "pet_deleted" to stringResource(R.string.pet_deleted),
        "appt_reason_required" to stringResource(R.string.appt_reason_required),
        "appt_date_past" to stringResource(R.string.appt_date_past),
        "appt_time_past" to stringResource(R.string.appt_time_past),
        "appt_saved" to stringResource(R.string.appt_saved),
        "appt_updated" to stringResource(R.string.appt_updated),
        "appt_deleted" to stringResource(R.string.appt_deleted),
        "vaccine_fields_required" to stringResource(R.string.vaccine_fields_required),
        "vaccine_saved" to stringResource(R.string.vaccine_saved),
        "vaccine_updated" to stringResource(R.string.vaccine_updated),
        "vaccine_deleted" to stringResource(R.string.vaccine_deleted),
    )

    val pendingMessage = listOfNotNull(
        authState.errorMessage?.let { PendingUiMessage("auth", it, true) },
        authState.successMessage?.let { PendingUiMessage("auth", it, false) },
        mascotaState.errorMessage?.let { PendingUiMessage("pet", it, true) },
        mascotaState.successMessage?.let { PendingUiMessage("pet", it, false) },
        citaState.errorMessage?.let { PendingUiMessage("appointment", it, true) },
        citaState.successMessage?.let { PendingUiMessage("appointment", it, false) },
        vacunaState.errorMessage?.let { PendingUiMessage("vaccine", it, true) },
        vacunaState.successMessage?.let { PendingUiMessage("vaccine", it, false) },
    ).firstOrNull()

    val retryLabel = stringResource(R.string.retry)
    val genericDatabaseMessage = stringResource(R.string.database_error)
    LaunchedEffect(pendingMessage) {
        val event = pendingMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = localizedMessages[event.code] ?: genericDatabaseMessage,
            actionLabel = if (event.error && event.code == "database_error") retryLabel else null,
            withDismissAction = true
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            when (event.source) {
                "auth" -> authViewModel.reintentar()
                "pet" -> mascotaViewModel.reintentar()
                "appointment" -> citaViewModel.reintentar()
                "vaccine" -> vacunaViewModel.reintentar()
            }
        }
        when (event.source) {
            "auth" -> authViewModel.clearMessage()
            "pet" -> mascotaViewModel.clearMessage()
            "appointment" -> citaViewModel.clearMessage()
            "vaccine" -> vacunaViewModel.clearMessage()
        }
    }

    // Barra inferior: 0 = Inicio, 1 = Mascotas, 2 = Citas, 3 = Perfil
    val goToTab: (Int) -> Unit = { index ->
        val route = when (index) {
            0 -> Routes.HOME
            1 -> Routes.PETS
            2 -> Routes.VACCINES
            3 -> Routes.PROFILE
            else -> null
        }
        if (route != null) {
            navController.navigate(route) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authMessage = authMessage,
                isLoading = authState.isLoading,
                onClearAuthMessage = authViewModel::limpiarError,
                onLoginClick = { email, password ->
                    authViewModel.limpiarError()
                    authViewModel.iniciarSesion(email, password)
                },
                onCreateAccountClick = { email, password ->
                    authViewModel.limpiarError()
                    authViewModel.crearCuenta(email, password)
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                userName = authState.userEmail?.substringBefore('@')?.replaceFirstChar { it.uppercase() }.orEmpty(),
                mascotas = mascotas,
                proximasCitas = citaState.citas,
                onTabSelected = goToTab,
                onScheduleClick = { navController.navigate(Routes.appointment()) },
                onAddPetClick = { navController.navigate(Routes.addPet()) },
                onViewDetailClick = { cita -> navController.navigate(Routes.appointment(cita.id)) }
            )
        }
        composable(Routes.PETS) {
            PetsScreen(
                mascotas = filteredMascotas,
                searchQuery = searchQuery,
                onSearchQueryChange = { mascotaViewModel.setSearchQuery(it) },
                totalMascotasCount = mascotas.size,
                onTabSelected = goToTab,
                onAddPetClick = { navController.navigate(Routes.addPet()) },
                onPetClick = { navController.navigate(Routes.petDetail(it)) },
            )
        }
        composable(
            route = Routes.PET_DETAIL,
            arguments = listOf(navArgument("petId") { type = NavType.IntType })
        ) { entry ->
            val petId = entry.arguments?.getInt("petId")
            val pet = mascotas.firstOrNull { it.id == petId }
            if (pet != null) {
                PetDetailScreen(
                    mascota = pet,
                    onBack = { navController.popBackStack() },
                    onDelete = { mascotaViewModel.eliminarMascota(pet) {
                        navController.popBackStack()
                    } },
                    onEdit = { navController.navigate(Routes.addPet(pet.id)) }
                )
            }
        }
        composable(
            route = Routes.ADD_PET,
            arguments = listOf(navArgument("mascotaId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            val mascotaId = entry.arguments?.getString("mascotaId")?.toIntOrNull()
            val mascotaEdicion = mascotas.firstOrNull { it.id == mascotaId }
            AddPetScreen(
                onBack = { navController.popBackStack() },
                onSave = { nombre, especie, raza, fechaNac, peso ->
                    if (mascotaEdicion == null) {
                        mascotaViewModel.guardarMascota(
                            nombre = nombre, especie = especie, raza = raza,
                            fechaNacimiento = fechaNac, pesoKg = peso,
                            onSuccess = { navController.popBackStack() }
                        )
                    } else {
                        mascotaViewModel.actualizarMascota(
                            mascotaEdicion.copy(
                                nombre = nombre, especie = especie, raza = raza,
                                fechaNacimiento = fechaNac, pesoKg = peso
                            ),
                            onSuccess = { navController.popBackStack() }
                        )
                    }
                },
                onTabSelected = goToTab,
                mascota = mascotaEdicion,
                errorMessage = mascotaState.errorMessage,
                isLoading = mascotaState.isLoading,
                onClearMessage = mascotaViewModel::clearMessage
            )
        }
        composable(Routes.VACCINES) {
            VaccinesScreen(
                mascotas = mascotas,
                vacunas = vacunaState.vacunasDeMascota,
                citas = citasDeMascotaSeleccionada,
                selectedMascotaId = selectedMascotaId,
                onMascotaSelect = { id ->
                    citaViewModel.selectMascotaId(id)
                    vacunaViewModel.seleccionarMascota(id)
                },
                onTabSelected = goToTab,
                onAddClick = {},
                onSaveVacuna = vacunaViewModel::guardarVacuna,
                onUpdateVacuna = vacunaViewModel::actualizarVacuna,
                onDeleteVacuna = vacunaViewModel::eliminarVacuna,
                onCitaClick = { cita -> navController.navigate(Routes.appointment(cita.id)) }
            )
        }
        composable(
            route = Routes.APPOINTMENT_EDIT,
            arguments = listOf(navArgument("citaId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { entry ->
            val citaId = entry.arguments?.getString("citaId")?.toIntOrNull()
            val citaEdicion = citaState.citas.firstOrNull { it.id == citaId }
            AppointmentScreen(
                mascotas = mascotas,
                onBack = { navController.popBackStack() },
                onDone = { mascotaId, motivo, fecha, hora, minuto, clinica, recordatorio ->
                    citaViewModel.guardarCita(
                        mascotaId = mascotaId,
                        motivo = motivo,
                        fecha = fecha,
                        hora = hora,
                        minuto = minuto,
                        clinica = clinica,
                        recordatorio = recordatorio,
                        onSuccess = { navController.popBackStack() }
                    )
                },
                onAddPetClick = { navController.navigate(Routes.addPet()) },
                onTabSelected = goToTab,
                cita = citaEdicion,
                onUpdate = { cita -> citaViewModel.actualizarCita(cita) { navController.popBackStack() } },
                onDelete = { cita -> citaViewModel.eliminarCita(cita) { navController.popBackStack() } },
                onClearMessage = citaViewModel::clearMessage
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = authState.userEmail?.substringBefore('@')?.replaceFirstChar { it.uppercase() }.orEmpty(),
                userEmail = authState.userEmail.orEmpty(),
                onTabSelected = goToTab,
                authError = when (authState.errorMessage) {
                    "auth_weak_password" -> stringResource(R.string.auth_weak_password)
                    "auth_fields_required" -> stringResource(R.string.auth_fields_required)
                    "auth_unavailable" -> stringResource(R.string.auth_unavailable)
                    "database_error" -> stringResource(R.string.database_error)
                    else -> null
                },
                onChangePassword = { password, done -> authViewModel.cambiarPassword(password, done) },
                onDeleteAccount = {
                    authViewModel.eliminarCuenta {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = dimensionResource(R.dimen.snackbar_bottom_offset))
    )
    }
}
