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

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PETS = "pets"
    const val ADD_PET = "add_pet"
    const val VACCINES = "vaccines"
    const val APPOINTMENT = "appointment"
    const val PROFILE = "profile"
    const val PET_DETAIL = "pet_detail/{petId}"
    fun petDetail(petId: Int) = "pet_detail/$petId"
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    mascotaViewModel: MascotaViewModel = hiltViewModel(),
    citaViewModel: CitaViewModel = hiltViewModel()
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

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val mascotas by mascotaViewModel.mascotas.collectAsStateWithLifecycle()
    val filteredMascotas by mascotaViewModel.filteredMascotas.collectAsStateWithLifecycle()
    val searchQuery by mascotaViewModel.searchQuery.collectAsStateWithLifecycle()

    val proximasCitas by citaViewModel.proximasCitas.collectAsStateWithLifecycle()
    val citasDeMascotaSeleccionada by citaViewModel.citasDeMascotaSeleccionada.collectAsStateWithLifecycle()
    val selectedMascotaId by citaViewModel.selectedMascotaId.collectAsStateWithLifecycle()

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
                proximasCitas = proximasCitas,
                onTabSelected = goToTab,
                onScheduleClick = { navController.navigate(Routes.APPOINTMENT) },
                onAddPetClick = { navController.navigate(Routes.ADD_PET) },
                onViewDetailClick = { goToTab(2) }
            )
        }
        composable(Routes.PETS) {
            PetsScreen(
                mascotas = filteredMascotas,
                searchQuery = searchQuery,
                onSearchQueryChange = { mascotaViewModel.setSearchQuery(it) },
                totalMascotasCount = mascotas.size,
                onTabSelected = goToTab,
                onAddPetClick = { navController.navigate(Routes.ADD_PET) },
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
                    } }
                )
            }
        }
        composable(Routes.ADD_PET) {
            AddPetScreen(
                onBack = { navController.popBackStack() },
                onSave = { nombre, especie, raza, fechaNac, peso ->
                    mascotaViewModel.guardarMascota(
                        nombre = nombre,
                        especie = especie,
                        raza = raza,
                        fechaNacimiento = fechaNac,
                        pesoKg = peso,
                        onSuccess = { navController.popBackStack() }
                    )
                },
                onTabSelected = goToTab
            )
        }
        composable(Routes.VACCINES) {
            VaccinesScreen(
                mascotas = mascotas,
                citas = citasDeMascotaSeleccionada,
                selectedMascotaId = selectedMascotaId,
                onMascotaSelect = { id -> citaViewModel.selectMascotaId(id) },
                onTabSelected = goToTab,
                onAddClick = { navController.navigate(Routes.APPOINTMENT) }
            )
        }
        composable(Routes.APPOINTMENT) {
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
                onAddPetClick = { navController.navigate(Routes.ADD_PET) },
                onTabSelected = goToTab
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = authState.userEmail?.substringBefore('@')?.replaceFirstChar { it.uppercase() }.orEmpty(),
                userEmail = authState.userEmail.orEmpty(),
                onTabSelected = goToTab,
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
