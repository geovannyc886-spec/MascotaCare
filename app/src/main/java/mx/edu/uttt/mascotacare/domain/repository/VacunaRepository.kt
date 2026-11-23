package mx.edu.uttt.mascotacare.domain.repository

import mx.edu.uttt.mascotacare.domain.model.Vacuna
import kotlinx.coroutines.flow.Flow

/** Contrato del dominio para consultar y modificar vacunas sin acoplarse a Room. */
interface VacunaRepository {
    fun obtenerTodas(): Flow<List<Vacuna>>
    suspend fun obtenerPorId(id: Int): Vacuna?
    fun obtenerDeMascota(mascotaId: Int): Flow<List<Vacuna>>
    fun obtenerProximasDeMascota(mascotaId: Int, desde: Long): Flow<List<Vacuna>>
    suspend fun insertar(vacuna: Vacuna): Long
    suspend fun actualizar(vacuna: Vacuna)
    suspend fun eliminar(vacuna: Vacuna)
}
