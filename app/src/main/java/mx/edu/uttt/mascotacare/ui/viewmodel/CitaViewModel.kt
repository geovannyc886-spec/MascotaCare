package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.Calendar
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
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.domain.repository.CitaRepository

data class CitaUiState(
    val citas: List<Cita> = emptyList(),
    val proximasCitas: List<Cita> = emptyList(),
    val citasDeMascota: List<Cita> = emptyList(),
    val selectedMascotaId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class CitaViewModel @Inject constructor(
    private val citaRepository: CitaRepository,
) : ViewModel() {

    // Observer: las consultas Room se comparten con stateIn y WhileSubscribed.
    val todasLasCitas: StateFlow<List<Cita>> = citaRepository.obtenerTodas()
        .map { lista -> lista.map { it.toEntity() } }
        .catch { error -> reportarError(error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val inicioRangoProximas = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val proximasCitas: StateFlow<List<Cita>> = citaRepository.obtenerProximas(inicioRangoProximas)
        .map { citas -> citas.map { it.toEntity() }.filter { momentoCita(it) >= System.currentTimeMillis() } }
        .catch { error -> reportarError(error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedMascotaId = MutableStateFlow<Int?>(null)
    val selectedMascotaId: StateFlow<Int?> = _selectedMascotaId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val citasDeMascotaSeleccionada: StateFlow<List<Cita>> = selectedMascotaId
        .flatMapLatest { id ->
            if (id == null) citaRepository.obtenerTodas() else citaRepository.obtenerDeMascota(id)
        }
        .map { lista -> lista.map { it.toEntity() } }
        .catch { error -> reportarError(error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(CitaUiState())
    val uiState: StateFlow<CitaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            todasLasCitas.collect { lista -> _uiState.update { it.copy(citas = lista, isLoading = false) } }
        }
        viewModelScope.launch {
            proximasCitas.collect { lista -> _uiState.update { it.copy(proximasCitas = lista, isLoading = false) } }
        }
        viewModelScope.launch {
            citasDeMascotaSeleccionada.collect {
                _uiState.update { state -> state.copy(citasDeMascota = it, isLoading = false) }
            }
        }
        viewModelScope.launch {
            selectedMascotaId.collect { id -> _uiState.update { it.copy(selectedMascotaId = id) } }
        }
    }

    // UDF: eventos de la UI entran por funciones públicas y producen nuevos estados.
    fun selectMascotaId(mascotaId: Int?) {
        _selectedMascotaId.value = mascotaId
    }

    fun guardarCita(
        mascotaId: Int,
        motivo: String,
        fecha: Long,
        hora: Int,
        minuto: Int,
        clinica: String,
        recordatorio: Boolean,
        onSuccess: () -> Unit = {},
    ) = viewModelScope.launch {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El motivo de la cita es obligatorio.") }
            return@launch
        }
        ejecutarOperacion {
            citaRepository.insertar(
                mx.edu.uttt.mascotacare.domain.model.Cita(
                    mascotaId = mascotaId, motivo = motivo.trim(), fecha = fecha,
                    hora = hora, minuto = minuto, clinica = clinica.trim(), recordatorio = recordatorio,
                ),
            )
        }
        if (_uiState.value.errorMessage == null) onSuccess()
    }

    fun actualizarCita(cita: Cita, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        ejecutarOperacion { citaRepository.actualizar(cita.toDomain()) }
        if (_uiState.value.errorMessage == null) onSuccess()
    }

    fun eliminarCita(cita: Cita, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        ejecutarOperacion { citaRepository.eliminar(cita.toDomain()) }
        if (_uiState.value.errorMessage == null) onSuccess()
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

    private fun Cita.toDomain() = mx.edu.uttt.mascotacare.domain.model.Cita(
        id = id, mascotaId = mascotaId, motivo = motivo, fecha = fecha,
        hora = hora, minuto = minuto, clinica = clinica, recordatorio = recordatorio,
    )

    private fun momentoCita(cita: Cita): Long = Calendar.getInstance().apply {
        timeInMillis = cita.fecha
        set(Calendar.HOUR_OF_DAY, cita.hora)
        set(Calendar.MINUTE, cita.minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
