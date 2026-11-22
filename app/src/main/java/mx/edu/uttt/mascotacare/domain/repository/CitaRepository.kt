package mx.edu.uttt.mascotacare.domain.repository

import mx.edu.uttt.mascotacare.domain.model.Cita
import kotlinx.coroutines.flow.Flow

interface CitaRepository {
    fun obtenerTodas(): Flow<List<Cita>>
    fun obtenerDeMascota(mascotaId: Int): Flow<List<Cita>>
    fun obtenerProximas(desde: Long): Flow<List<Cita>>
    suspend fun insertar(cita: Cita): Long
    suspend fun actualizar(cita: Cita)
    suspend fun eliminar(cita: Cita)
}
