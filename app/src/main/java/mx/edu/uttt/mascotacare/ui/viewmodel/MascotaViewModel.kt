package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import mx.edu.uttt.mascotacare.util.isDateAfterToday
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.uttt.mascotacare.domain.model.Mascota
import mx.edu.uttt.mascotacare.domain.repository.MascotaRepository

data class MascotaUiState(
    val mascotas: List<Mascota> = emptyList(),
    val mascotasFiltradas: List<Mascota> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
// Hilt inyecta el contrato MascotaRepository del dominio.
class MascotaViewModel @Inject constructor(
    private val mascotaRepository: MascotaRepository,
) : ViewModel() {
    private var retryAction: (() -> Unit)? = null

    // Observer: stateIn comparte las consultas mientras haya observadores activos.
    val mascotas: StateFlow<List<Mascota>> = mascotaRepository.obtenerTodas()
        .catch { error -> reportarError(error, "database_load_error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredMascotas: StateFlow<List<Mascota>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) mascotaRepository.obtenerTodas()
            else mascotaRepository.buscarPorNombre(query.trim())
        }
        .catch { error -> reportarError(error, "database_load_error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(MascotaUiState())
    val uiState: StateFlow<MascotaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mascotas.collect { lista -> _uiState.update { it.copy(mascotas = lista, isLoading = false) } }
        }
        viewModelScope.launch {
            filteredMascotas.collect { lista ->
                _uiState.update { it.copy(mascotasFiltradas = lista, isLoading = false) }
            }
        }
        viewModelScope.launch {
            searchQuery.collect { query -> _uiState.update { it.copy(searchQuery = query) } }
        }
    }

    // UDF: las acciones entran por funciones y actualizan un UiState de solo lectura.
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearMessage() { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }
    fun reintentar() { retryAction?.invoke() }

    fun guardarMascota(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: Long?,
        pesoKg: Double?,
        onSuccess: () -> Unit = {},
    ): Job = viewModelScope.launch {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isBlank()) {
            _uiState.update { it.copy(errorMessage = "pet_name_required") }
            return@launch
        }
        if (nombreLimpio.length > 20) {
            _uiState.update { it.copy(errorMessage = "pet_name_too_long") }
            return@launch
        }
        if (fechaNacimiento?.let(::isDateAfterToday) == true) {
            _uiState.update { it.copy(errorMessage = "pet_birth_future") }
            return@launch
        }
        if (pesoKg == null || !pesoKg.isFinite() || pesoKg <= 0.0) {
            _uiState.update { it.copy(errorMessage = "pet_weight_invalid") }
            return@launch
        }
        if (_uiState.value.mascotas.any { it.nombre.equals(nombreLimpio, true) && it.especie.equals(especie, true) }) {
            _uiState.update { it.copy(errorMessage = "pet_duplicate") }
            return@launch
        }
        val op: suspend () -> Unit = {
            mascotaRepository.insertar(
                mx.edu.uttt.mascotacare.domain.model.Mascota(
                    nombre = nombreLimpio, especie = especie,
                    raza = raza.trim(), fechaNacimiento = fechaNacimiento, pesoKg = pesoKg,
                ),
            )
            Unit
        }
        retryAction = { guardarMascota(nombre, especie, raza, fechaNacimiento, pesoKg, onSuccess) }
        if (ejecutarOperacion(op, "pet_saved")) { retryAction = null; onSuccess() }
    }

    fun actualizarMascota(mascota: Mascota, onSuccess: () -> Unit = {}): Job = viewModelScope.launch {
        if (mascota.nombre.isBlank() || mascota.nombre.trim().length > 20) {
            _uiState.update { it.copy(errorMessage = if (mascota.nombre.isBlank()) "pet_name_required" else "pet_name_too_long") }
            return@launch
        }
        if (mascota.fechaNacimiento?.let(::isDateAfterToday) == true) { _uiState.update { it.copy(errorMessage = "pet_birth_future") }; return@launch }
        if (mascota.pesoKg == null || !mascota.pesoKg.isFinite() || mascota.pesoKg <= 0.0) { _uiState.update { it.copy(errorMessage = "pet_weight_invalid") }; return@launch }
        if (_uiState.value.mascotas.any { it.id != mascota.id && it.nombre.equals(mascota.nombre.trim(), true) && it.especie.equals(mascota.especie, true) }) {
            _uiState.update { it.copy(errorMessage = "pet_duplicate") }; return@launch
        }
        retryAction = { actualizarMascota(mascota, onSuccess) }
        if (ejecutarOperacion({ mascotaRepository.actualizar(mascota) }, "pet_updated")) { retryAction = null; onSuccess() }
    }

    fun eliminarMascota(mascota: Mascota, onSuccess: () -> Unit = {}): Job = viewModelScope.launch {
        retryAction = { eliminarMascota(mascota, onSuccess) }
        if (ejecutarOperacion({ mascotaRepository.eliminar(mascota) }, "pet_deleted")) { retryAction = null; onSuccess() }
    }

    private suspend fun ejecutarOperacion(operacion: suspend () -> Unit, success: String): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        return try {
            operacion()
            _uiState.update { it.copy(successMessage = success) }
            true
        } catch (error: Exception) {
            reportarError(error)
            false
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun reportarError(error: Throwable, code: String = "database_error") {
        _uiState.update { it.copy(errorMessage = code) }
    }

}
