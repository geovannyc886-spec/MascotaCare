package mx.edu.uttt.mascotacare.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import mx.edu.uttt.mascotacare.data.database.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Usuario?
}
