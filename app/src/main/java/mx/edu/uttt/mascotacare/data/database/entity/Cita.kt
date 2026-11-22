package mx.edu.uttt.mascotacare.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de la tabla de citas (cada cita pertenece a una mascota).
 */
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascota_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mascota_id")],
)
data class Cita(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "mascota_id")
    val mascotaId: Int,

    val motivo: String,

    val fecha: Long,

    val hora: Int,

    val minuto: Int,

    val clinica: String = "",

    val recordatorio: Boolean = true,
)
