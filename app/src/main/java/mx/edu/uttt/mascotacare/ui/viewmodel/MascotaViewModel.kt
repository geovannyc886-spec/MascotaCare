package mx.edu.uttt.mascotacare.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.viewModelScope
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import mx.edu.uttt.mascotacare.domain.repository.MascotaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MascotaViewModel @Inject constructor(private val mascotaRepository: MascotaRepository) : ViewModel() {

    val mascotas: StateFlow<List<Mascota>> = mascotaRepository.obtenerTodas().map { it.map { mascota -> mascota.toEntity() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredMascotas: StateFlow<List<Mascota>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                mascotaRepository.obtenerTodas()
            } else {
                mascotaRepository.buscarPorNombre(query.trim())
            }
        }
        .map { it.map { mascota -> mascota.toEntity() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun guardarMascota(
        nombre: String,
        especie: String,
        raza: String,
        fechaNacimiento: Long?,
        pesoKg: Double?,
        onSuccess: () -> Unit = {}
    ) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            val mascota = mx.edu.uttt.mascotacare.domain.model.Mascota(
                nombre = nombre.trim(),
                especie = especie.ifBlank { "Otro" },
                raza = raza.trim(),
                fechaNacimiento = fechaNacimiento,
                pesoKg = pesoKg
            )
            mascotaRepository.insertar(mascota)
            onSuccess()
        }
    }

    fun eliminarMascota(mascota: Mascota, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            mascotaRepository.eliminar(mascota.let {
                mx.edu.uttt.mascotacare.domain.model.Mascota(it.id, it.nombre, it.especie, it.raza, it.fechaNacimiento, it.pesoKg, it.fotoUri)
            })
            onSuccess()
        }
    }
}
