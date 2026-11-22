package mx.edu.uttt.mascotacare.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import mx.edu.uttt.mascotacare.data.database.entity.Mascota
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {

    @Insert
    suspend fun insertar(mascota: Mascota): Long

    @Update
    suspend fun actualizar(mascota: Mascota)

    @Delete
    suspend fun eliminar(mascota: Mascota)

    @Query("SELECT * FROM mascotas ORDER BY nombre")
    fun obtenerTodas(): Flow<List<Mascota>>

    @Query("SELECT * FROM mascotas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Mascota?

    @Query("SELECT * FROM mascotas WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    fun buscarPorNombre(texto: String): Flow<List<Mascota>>
}
