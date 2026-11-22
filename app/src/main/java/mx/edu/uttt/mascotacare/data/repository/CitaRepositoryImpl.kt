package mx.edu.uttt.mascotacare.data.repository

import mx.edu.uttt.mascotacare.data.database.dao.CitaDao
import mx.edu.uttt.mascotacare.data.mapper.toDomain
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.domain.model.Cita
import mx.edu.uttt.mascotacare.domain.repository.CitaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CitaRepositoryImpl @Inject constructor(private val citaDao: CitaDao) : CitaRepository {
    override fun obtenerTodas(): Flow<List<Cita>> =
        citaDao.obtenerTodas().map { lista -> lista.map { it.toDomain() } }

    override fun obtenerDeMascota(mascotaId: Int): Flow<List<Cita>> =
        citaDao.obtenerDeMascota(mascotaId).map { lista -> lista.map { it.toDomain() } }

    override fun obtenerProximas(desde: Long): Flow<List<Cita>> =
        citaDao.obtenerProximas(desde).map { lista -> lista.map { it.toDomain() } }

    override suspend fun insertar(cita: Cita): Long = citaDao.insertar(cita.toEntity())

    override suspend fun actualizar(cita: Cita) = citaDao.actualizar(cita.toEntity())

    override suspend fun eliminar(cita: Cita) = citaDao.eliminar(cita.toEntity())
}
