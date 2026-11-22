package mx.edu.uttt.mascotacare.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import mx.edu.uttt.mascotacare.data.database.entity.Vacuna
import kotlinx.coroutines.flow.Flow

@Dao
interface VacunaDao {
    @Insert
    suspend fun insertar(vacuna: Vacuna): Long

    @Update
    suspend fun actualizar(vacuna: Vacuna)

    @Delete
    suspend fun eliminar(vacuna: Vacuna)

    @Query("SELECT * FROM vacunas ORDER BY fecha, nombre")
    fun obtenerTodas(): Flow<List<Vacuna>>

    @Query("SELECT * FROM vacunas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Vacuna?

    @Query("SELECT * FROM vacunas WHERE mascota_id = :mascotaId ORDER BY fecha, nombre")
    fun obtenerDeMascota(mascotaId: Int): Flow<List<Vacuna>>

    @Query("SELECT * FROM vacunas WHERE mascota_id = :mascotaId AND fecha >= :desde ORDER BY fecha, nombre")
    fun obtenerProximasDeMascota(mascotaId: Int, desde: Long): Flow<List<Vacuna>>
}
