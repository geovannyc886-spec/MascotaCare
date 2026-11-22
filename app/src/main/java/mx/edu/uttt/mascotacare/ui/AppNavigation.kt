package mx.edu.uttt.mascotacare.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
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

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    mascotaViewModel: MascotaViewModel = hiltViewModel(),
    citaViewModel: CitaViewModel = hiltViewModel(),
    vacunaViewModel: VacunaViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val loginFailedMessage = stringResource(R.string.auth_invalid_credentials)
    val accountExistsMessage = stringResource(R.string.auth_account_exists)
    val authErrorMessage = stringResource(R.string.auth_unavailable)
    val authMessage = when (authState.errorMessage) {
        "auth_invalid_credentials" -> loginFailedMessage
        "auth_account_exists" -> accountExistsMessage
        "auth_unavailable" -> authErrorMessage
        else -> authState.errorMessage
    }
    val petNameRequired = stringResource(R.string.add_pet_name_error)
    val petNameTooLong = stringResource(R.string.add_pet_name_max_error)
    val petSaveError = stringResource(R.string.pet_save_error)

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
                errorMessage = when (mascotaState.errorMessage) {
                    "add_pet_name_required" -> petNameRequired
                    "add_pet_name_max_error" -> petNameTooLong
                    null -> null
                    else -> petSaveError
                },
                isLoading = mascotaState.isLoading
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
                onDelete = { cita -> citaViewModel.eliminarCita(cita) { navController.popBackStack() } }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = authState.userEmail?.substringBefore('@')?.replaceFirstChar { it.uppercase() }.orEmpty(),
                userEmail = authState.userEmail.orEmpty(),
                onTabSelected = goToTab,
                authError = when (authState.errorMessage) {
                    "auth_short_password" -> stringResource(R.string.login_short_password)
                    "auth_unavailable" -> stringResource(R.string.auth_unavailable)
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
}
