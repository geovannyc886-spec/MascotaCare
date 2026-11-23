package mx.edu.uttt.mascotacare.data.repository

import mx.edu.uttt.mascotacare.data.database.dao.MascotaDao
import mx.edu.uttt.mascotacare.data.mapper.toDomain
import mx.edu.uttt.mascotacare.data.mapper.toEntity
import mx.edu.uttt.mascotacare.domain.model.Mascota
import mx.edu.uttt.mascotacare.domain.repository.MascotaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Implementa el contrato de domain y traduce entre Room y los modelos del dominio. */
class MascotaRepositoryImpl @Inject constructor(private val mascotaDao: MascotaDao) : MascotaRepository {
    override fun obtenerTodas(): Flow<List<Mascota>> =
        mascotaDao.obtenerTodas().map { lista -> lista.map { it.toDomain() } }

    override fun buscarPorNombre(texto: String): Flow<List<Mascota>> =
        mascotaDao.buscarPorNombre(texto).map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtenerPorId(id: Int): Mascota? =
        mascotaDao.obtenerPorId(id)?.toDomain()

    override suspend fun insertar(mascota: Mascota): Long =
        mascotaDao.insertar(mascota.toEntity())

    override suspend fun actualizar(mascota: Mascota) =
        mascotaDao.actualizar(mascota.toEntity())

    override suspend fun eliminar(mascota: Mascota) =
        mascotaDao.eliminar(mascota.toEntity())
}
