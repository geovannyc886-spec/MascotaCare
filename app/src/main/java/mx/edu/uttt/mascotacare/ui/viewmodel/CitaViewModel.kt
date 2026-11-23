package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import mx.edu.uttt.mascotacare.util.dateTimeAtLocalTime
import mx.edu.uttt.mascotacare.util.isDateBeforeToday
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
import mx.edu.uttt.mascotacare.domain.model.Cita
import mx.edu.uttt.mascotacare.domain.repository.CitaRepository

data class CitaUiState(
    val citas: List<Cita> = emptyList(),
    val proximasCitas: List<Cita> = emptyList(),
    val citasDeMascota: List<Cita> = emptyList(),
    val selectedMascotaId: Int? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
// Hilt inyecta el contrato CitaRepository del dominio.
class CitaViewModel @Inject constructor(
    private val citaRepository: CitaRepository,
) : ViewModel() {
    private var retryAction: (() -> Unit)? = null

    // Observer: las consultas Room se comparten con stateIn y WhileSubscribed.
    val todasLasCitas: StateFlow<List<Cita>> = citaRepository.obtenerTodas()
        .catch { error -> reportarError(error, "database_load_error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val inicioRangoProximas = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val proximasCitas: StateFlow<List<Cita>> = citaRepository.obtenerProximas(inicioRangoProximas)
        .map { citas -> citas.filter { momentoCita(it) >= System.currentTimeMillis() } }
        .catch { error -> reportarError(error, "database_load_error") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedMascotaId = MutableStateFlow<Int?>(null)
    val selectedMascotaId: StateFlow<Int?> = _selectedMascotaId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val citasDeMascotaSeleccionada: StateFlow<List<Cita>> = selectedMascotaId
        .flatMapLatest { id ->
            if (id == null) citaRepository.obtenerTodas() else citaRepository.obtenerDeMascota(id)
        }
        .catch { error -> reportarError(error, "database_load_error") }
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

    fun clearMessage() { _uiState.update { it.copy(errorMessage = null, successMessage = null) } }
    fun reintentar() { retryAction?.invoke() }

    fun guardarCita(
        mascotaId: Int,
        motivo: String,
        fecha: Long,
        hora: Int,
        minuto: Int,
        clinica: String,
        recordatorio: Boolean,
        onSuccess: () -> Unit = {},
    ): Job = viewModelScope.launch {
        if (motivo.isBlank()) {
            _uiState.update { it.copy(errorMessage = "appt_reason_required") }
            return@launch
        }
        if (momentoCita(fecha, hora, minuto) < System.currentTimeMillis()) {
            _uiState.update { it.copy(errorMessage = if (fechaEsPasada(fecha)) "appt_date_past" else "appt_time_past") }
            return@launch
        }
        retryAction = { guardarCita(mascotaId, motivo, fecha, hora, minuto, clinica, recordatorio, onSuccess) }
        if (ejecutarOperacion("appt_saved") {
            citaRepository.insertar(
                mx.edu.uttt.mascotacare.domain.model.Cita(
                    mascotaId = mascotaId, motivo = motivo.trim(), fecha = fecha,
                    hora = hora, minuto = minuto, clinica = clinica.trim(), recordatorio = recordatorio,
                ),
            )
        }) { retryAction = null; onSuccess() }
    }

    fun actualizarCita(cita: Cita, onSuccess: () -> Unit = {}): Job = viewModelScope.launch {
        if (cita.motivo.isBlank()) { _uiState.update { it.copy(errorMessage = "appt_reason_required") }; return@launch }
        if (momentoCita(cita.fecha, cita.hora, cita.minuto) < System.currentTimeMillis()) { _uiState.update { it.copy(errorMessage = if (fechaEsPasada(cita.fecha)) "appt_date_past" else "appt_time_past") }; return@launch }
        retryAction = { actualizarCita(cita, onSuccess) }
        if (ejecutarOperacion("appt_updated") { citaRepository.actualizar(cita) }) { retryAction = null; onSuccess() }
    }

    fun eliminarCita(cita: Cita, onSuccess: () -> Unit = {}): Job = viewModelScope.launch {
        retryAction = { eliminarCita(cita, onSuccess) }
        if (ejecutarOperacion("appt_deleted") { citaRepository.eliminar(cita) }) { retryAction = null; onSuccess() }
    }

    private suspend fun ejecutarOperacion(successMessage: String, operacion: suspend () -> Unit): Boolean {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        try {
            operacion()
            _uiState.update { it.copy(successMessage = successMessage) }
            return true
        } catch (error: Exception) {
            reportarError(error)
            return false
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun reportarError(error: Throwable, code: String = "database_error") {
        _uiState.update { it.copy(errorMessage = code) }
    }

    private fun momentoCita(cita: Cita): Long = dateTimeAtLocalTime(cita.fecha, cita.hora, cita.minuto)

    private fun momentoCita(fecha: Long, hora: Int, minuto: Int): Long = dateTimeAtLocalTime(fecha, hora, minuto)

    private fun fechaEsPasada(fecha: Long): Boolean = isDateBeforeToday(fecha)
}
