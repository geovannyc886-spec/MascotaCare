package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.domain.repository.MascotaRepository

data class MascotaUiState(
    val mascotas: List<Mascota> = emptyList(),
    val mascotasFiltradas: List<Mascota> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class MascotaViewModel @Inject constructor(
    private val mascotaRepository: MascotaRepository,
) : ViewModel() {

    // Observer: stateIn comparte las consultas mientras haya observadores activos.
    val mascotas: StateFlow<List<Mascota>> = mascotaRepository.obtenerTodas()
        .map { lista -> lista.map { it.toEntity() } }
        .catch { error -> reportarError(error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredMascotas: StateFlow<List<Mascota>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) mascotaRepository.obtenerTodas()
            else mascotaRepository.buscarPorNombre(query.trim())
        }
        .map { lista -> lista.map { it.toEntity() } }
        .catch { error -> reportarError(error) }
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

    fun guardarMascota(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: Long?,
        pesoKg: Double?,
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        if (nombre.isBlank()) {
            _uiState.update { it.copy(errorMessage = "add_pet_name_required") }
            return@launch
        }
        if (nombre.trim().length > 20) {
            _uiState.update { it.copy(errorMessage = "add_pet_name_max_error") }
            return@launch
        }
        if (ejecutarOperacion {
            mascotaRepository.insertar(
                mx.edu.uttt.mascotacare.domain.model.Mascota(
                    nombre = nombre.trim(), especie = especie.ifBlank { "Otro" },
                    raza = raza.trim(), fechaNacimiento = fechaNacimiento, pesoKg = pesoKg,
                ),
            )
        }) onSuccess()
    }

    fun actualizarMascota(mascota: Mascota, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (mascota.nombre.isBlank() || mascota.nombre.length > 20) {
            _uiState.update { it.copy(errorMessage = if (mascota.nombre.isBlank()) "add_pet_name_required" else "add_pet_name_max_error") }
            return@launch
        }
        if (ejecutarOperacion {
            mascotaRepository.actualizar(mascota.toDomain())
        }) onSuccess()
    }

    fun eliminarMascota(mascota: Mascota, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        if (ejecutarOperacion { mascotaRepository.eliminar(mascota.toDomain()) }) onSuccess()
    }

    private suspend fun ejecutarOperacion(operacion: suspend () -> Unit): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        return try {
            operacion()
            true
        } catch (error: Exception) {
            reportarError(error)
            false
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun reportarError(error: Throwable) {
        _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo completar la operación.") }
    }

    private fun Mascota.toDomain() = mx.edu.uttt.mascotacare.domain.model.Mascota(
        id = id, nombre = nombre, especie = especie, raza = raza,
        fechaNacimiento = fechaNacimiento, pesoKg = pesoKg, fotoUri = fotoUri,
    )
}
