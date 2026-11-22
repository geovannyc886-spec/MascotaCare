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
import mx.edu.uttt.mascotacare.domain.model.Vacuna
import mx.edu.uttt.mascotacare.domain.repository.VacunaRepository

data class VacunaUiState(
    val vacunas: List<Vacuna> = emptyList(),
    val vacunasDeMascota: List<Vacuna> = emptyList(),
    val mascotaSeleccionadaId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class VacunaViewModel @Inject constructor(
    private val vacunaRepository: VacunaRepository,
) : ViewModel() {

    // Observer: ambas consultas se exponen como StateFlow con suscripción controlada.
    val vacunas: StateFlow<List<Vacuna>> = vacunaRepository.obtenerTodas()
        .catch { reportarError(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mascotaSeleccionadaId = MutableStateFlow<Int?>(null)
    val mascotaSeleccionadaId: StateFlow<Int?> = _mascotaSeleccionadaId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val vacunasDeMascota: StateFlow<List<Vacuna>> = mascotaSeleccionadaId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else vacunaRepository.obtenerDeMascota(id)
        }
        .catch { reportarError(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(VacunaUiState())
    val uiState: StateFlow<VacunaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            vacunas.collect { lista -> _uiState.update { it.copy(vacunas = lista, isLoading = false) } }
        }
        viewModelScope.launch {
            vacunasDeMascota.collect { lista ->
                _uiState.update { it.copy(vacunasDeMascota = lista, isLoading = false) }
            }
        }
        viewModelScope.launch {
            mascotaSeleccionadaId.collect { id -> _uiState.update { it.copy(mascotaSeleccionadaId = id) } }
        }
    }

    // UDF: selección y cambios CRUD son acciones explícitas que actualizan el estado.
    fun seleccionarMascota(id: Int?) {
        _mascotaSeleccionadaId.value = id
    }

    fun guardarVacuna(vacuna: Vacuna) = viewModelScope.launch {
        ejecutarOperacion { vacunaRepository.insertar(vacuna) }
    }

    fun actualizarVacuna(vacuna: Vacuna) = viewModelScope.launch {
        ejecutarOperacion { vacunaRepository.actualizar(vacuna) }
    }

    fun eliminarVacuna(vacuna: Vacuna) = viewModelScope.launch {
        ejecutarOperacion { vacunaRepository.eliminar(vacuna) }
    }

    private suspend fun ejecutarOperacion(operacion: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            operacion()
        } catch (error: Exception) {
            reportarError(error)
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun reportarError(error: Throwable) {
        _uiState.update { it.copy(errorMessage = error.message ?: "No se pudo completar la operación.") }
    }
}
