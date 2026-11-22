package mx.edu.uttt.mascotacare.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import mx.edu.uttt.mascotacare.data.database.entity.Cita
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {

    @Insert
    suspend fun insertar(cita: Cita): Long

    @Update
    suspend fun actualizar(cita: Cita)

    @Delete
    suspend fun eliminar(cita: Cita)

    @Query("SELECT * FROM citas ORDER BY fecha, hora, minuto")
    fun obtenerTodas(): Flow<List<Cita>>

    @Query("SELECT * FROM citas WHERE mascota_id = :mascotaId ORDER BY fecha, hora, minuto")
    fun obtenerDeMascota(mascotaId: Int): Flow<List<Cita>>

    /** Próximas citas a partir de una fecha (en milisegundos). */
    @Query("SELECT * FROM citas WHERE fecha >= :desde ORDER BY fecha, hora, minuto")
    fun obtenerProximas(desde: Long): Flow<List<Cita>>
}
