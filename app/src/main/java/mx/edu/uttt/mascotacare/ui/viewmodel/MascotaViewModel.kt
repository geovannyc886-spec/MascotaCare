package mx.edu.uttt.mascotacare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import mx.edu.uttt.mascotacare.data.database.AppDatabase
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MascotaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val mascotaDao = db.mascotaDao()

    val mascotas: StateFlow<List<Mascota>> = mascotaDao.obtenerTodas()
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
                mascotaDao.obtenerTodas()
            } else {
                mascotaDao.buscarPorNombre(query.trim())
            }
        }
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
            val mascota = Mascota(
                nombre = nombre.trim(),
                especie = especie.ifBlank { "Otro" },
                raza = raza.trim(),
                fechaNacimiento = fechaNacimiento,
                pesoKg = pesoKg
            )
            mascotaDao.insertar(mascota)
            onSuccess()
        }
    }

    fun eliminarMascota(mascota: Mascota, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            mascotaDao.eliminar(mascota)
            onSuccess()
        }
    }
}
