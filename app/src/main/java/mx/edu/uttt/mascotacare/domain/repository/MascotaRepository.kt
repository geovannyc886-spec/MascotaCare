package mx.edu.uttt.mascotacare.domain.repository

import mx.edu.uttt.mascotacare.domain.model.Mascota
import kotlinx.coroutines.flow.Flow

interface MascotaRepository {
    fun obtenerTodas(): Flow<List<Mascota>>
    fun buscarPorNombre(texto: String): Flow<List<Mascota>>
    suspend fun obtenerPorId(id: Int): Mascota?
    suspend fun insertar(mascota: Mascota): Long
    suspend fun actualizar(mascota: Mascota)
    suspend fun eliminar(mascota: Mascota)
}
