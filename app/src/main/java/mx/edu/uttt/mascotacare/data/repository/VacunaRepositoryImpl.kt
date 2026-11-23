package mx.edu.uttt.mascotacare.data.repository

import mx.edu.uttt.mascotacare.data.database.dao.VacunaDao
import mx.edu.uttt.mascotacare.data.mapper.toDomain
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.domain.model.Vacuna
import mx.edu.uttt.mascotacare.domain.repository.VacunaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Implementa el contrato de domain y traduce entre Room y los modelos del dominio. */
class VacunaRepositoryImpl @Inject constructor(private val vacunaDao: VacunaDao) : VacunaRepository {
    override fun obtenerTodas(): Flow<List<Vacuna>> =
        vacunaDao.obtenerTodas().map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtenerPorId(id: Int): Vacuna? =
        vacunaDao.obtenerPorId(id)?.toDomain()

    override fun obtenerDeMascota(mascotaId: Int): Flow<List<Vacuna>> =
        vacunaDao.obtenerDeMascota(mascotaId).map { lista -> lista.map { it.toDomain() } }

    override fun obtenerProximasDeMascota(mascotaId: Int, desde: Long): Flow<List<Vacuna>> =
        vacunaDao.obtenerProximasDeMascota(mascotaId, desde)
            .map { lista -> lista.map { it.toDomain() } }

    override suspend fun insertar(vacuna: Vacuna): Long =
        vacunaDao.insertar(vacuna.toEntity())

    override suspend fun actualizar(vacuna: Vacuna) =
        vacunaDao.actualizar(vacuna.toEntity())

    override suspend fun eliminar(vacuna: Vacuna) =
        vacunaDao.eliminar(vacuna.toEntity())
}
