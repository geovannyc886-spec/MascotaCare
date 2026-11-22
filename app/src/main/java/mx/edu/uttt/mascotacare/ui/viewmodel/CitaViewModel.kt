package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import mx.edu.uttt.mascotacare.domain.repository.CitaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CitaViewModel @Inject constructor(private val citaRepository: CitaRepository) : ViewModel() {

    val todasLasCitas: StateFlow<List<Cita>> = citaRepository.obtenerTodas().map { it.map { cita -> cita.toEntity() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val inicioRangoProximas = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val proximasCitas: StateFlow<List<Cita>> = citaRepository.obtenerProximas(inicioRangoProximas)
        .map { citas -> citas.map { it.toEntity() }.filter { momentoCita(it) >= System.currentTimeMillis() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMascotaId = MutableStateFlow<Int?>(null)
    val selectedMascotaId: StateFlow<Int?> = _selectedMascotaId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val citasDeMascotaSeleccionada: StateFlow<List<Cita>> = _selectedMascotaId
        .flatMapLatest { id ->
            if (id != null) {
                citaRepository.obtenerDeMascota(id)
            } else {
                citaRepository.obtenerTodas()
            }
        }
        .map { it.map { cita -> cita.toEntity() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
        onSuccess: () -> Unit = {}
    ) {
        if (motivo.isBlank()) return
        viewModelScope.launch {
            val cita = mx.edu.uttt.mascotacare.domain.model.Cita(
                mascotaId = mascotaId,
                motivo = motivo.trim(),
                fecha = fecha,
                hora = hora,
                minuto = minuto,
                clinica = clinica.trim(),
                recordatorio = recordatorio
            )
            citaRepository.insertar(cita)
            onSuccess()
        }
    }

    fun eliminarCita(cita: Cita, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            citaRepository.eliminar(
                mx.edu.uttt.mascotacare.domain.model.Cita(
                    id = cita.id,
                    mascotaId = cita.mascotaId,
                    motivo = cita.motivo,
                    fecha = cita.fecha,
                    hora = cita.hora,
                    minuto = cita.minuto,
                    clinica = cita.clinica,
                    recordatorio = cita.recordatorio
                )
            )
            onSuccess()
        }
    }

    private fun momentoCita(cita: Cita): Long = Calendar.getInstance().apply {
        timeInMillis = cita.fecha
        set(Calendar.HOUR_OF_DAY, cita.hora)
        set(Calendar.MINUTE, cita.minuto)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
