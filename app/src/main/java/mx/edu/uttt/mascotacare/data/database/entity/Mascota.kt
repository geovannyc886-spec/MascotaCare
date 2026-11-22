package mx.edu.uttt.mascotacare.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de la tabla de mascotas.
 */
@Entity(tableName = "mascotas")
data class Mascota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    val especie: String,

    val raza: String = "",

    @ColumnInfo(name = "fecha_nacimiento")
    val fechaNacimiento: Long? = null,

    @ColumnInfo(name = "peso_kg")
    val pesoKg: Double? = null,

    @ColumnInfo(name = "foto_uri")
    val fotoUri: String? = null,
)
